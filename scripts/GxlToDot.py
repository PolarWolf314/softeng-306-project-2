from typing import List, Dict
from itertools import groupby
import xml.etree.ElementTree as ET
import os


class Node:
    def __init__(self, node: ET.Element):
        """ 
        Parses an GXL element into a Node object.
        """
        self.id: str = node.get('id')

        attributes = parse_attributes(node)
        self.start_time: int = attributes["Start time"]
        self.weight: int = attributes["Weight"]
        self.finish_time: int = attributes["Finish time"]
        self.processor_index: int = attributes["Processor"]

    def to_input_dot_node(self) -> str:
        """
        Returns a string representation of this node in the input DOT graph format (Just the weight attribute).
        """
        return f'{self.id} [Weight={self.weight}];'

    def to_output_dot_node(self) -> str:
        """
        Returns a string representation of this node in the output DOT graph format (Weight, Start and Processor attributes).
        """
        # +1 on the processor as we want to start at 1 instead of 0
        return f'{self.id} [Weight={self.weight},Start={self.start_time},Processor={self.processor_index + 1}];'

    def __str__(self) -> str:
        """
        Returns a string representation of this node for debugging purposes.
        """
        return f'Node[id={self.id}, start_time={self.start_time}, weight={self.weight}, finish_time={self.finish_time}, processor={self.processor_index}]'


class Edge:
    def __init__(self, edge: ET.Element):
        """
        Parses a GXL element into an Edge object.
        """
        self.source: str = edge.get('from')
        self.target: str = edge.get('to')

        attributes = parse_attributes(edge)
        self.weight: int = attributes["Weight"]

    def to_dot_edge(self) -> str:
        """
        Returns a string representation of this edge in the DOT graph format.
        """
        return f'{self.source} -> {self.target} [Weight={self.weight}];'

    def __str__(self) -> str:
        """
        Returns a string representation of this edge for debugging purposes.
        """
        return f'Edge[source={self.source}, target={self.target}, weight={self.weight}]'


class Graph:
    def __init__(self, filen_path: str):
        """
        Parses a GXL file at the given file path into a Graph object.
        """
        tree = ET.parse(filen_path)
        root = tree.getroot()
        graph = root.find('graph')

        # The id of the graph is not unique unless used in combination with the target system, 
        # which is just the filename.
        self.name = os.path.basename(filen_path).replace('.gxl', '')

        attributes = parse_attributes(graph)
        self.optimal_schedule_end_time: int = attributes['Total schedule length']

        self.nodes: List[Node] = []
        self.edges: List[Edge] = []

        for node_tag in graph.findall('node'):
            node = Node(node_tag)
            self.nodes.append(node)

        for edge_tag in graph.findall('edge'):
            edge = Edge(edge_tag)
            self.edges.append(edge)

        # Determine the number of unique processors in this graph
        self.processor_count = max([node.processor_index for node in self.nodes]) + 1

    def get_filename(self) -> str:
        """
        Returns the filename of the DOT representation of this graph.
        """
        return self.name + '.dot'

    def find_outgoing_edges(self, node: Node) -> List[Edge]:
        """
        Returns a list of all edges that have the given node as their source.
        """
        return [edge for edge in self.edges if edge.source == node.id]


    def get_processor_end_times(self) -> List[int]:
        """
        Returns the latest end time for each processor in this graph. The index of the list corresponds
        to the index of the processor.
        """
        processor_end_times = [0 for _ in range(self.processor_count)]

        for node in self.nodes:
            if node.processor_index >= len(processor_end_times):
                print(f'❗ Processor index out of bounds for graph {self.name}')
                return processor_end_times

            processor_end_times[node.processor_index] = max(
                processor_end_times[node.processor_index], node.finish_time)

        return processor_end_times

    def to_input_dot_graph(self) -> str:
        """
        Returns a string representation of the input DOT representation of this graph.
        """
        output = f'digraph "{self.name}" {{\n'
        output += '\n'.join([node.to_input_dot_node() for node in self.nodes]) + '\n'
        output += '\n'.join([edge.to_dot_edge() for edge in self.edges]) + '\n'
        output += '}'

        return output

    def to_output_dot_graph(self) -> str:
        """
        Builds a string of the DOT representation of this graph. Care has been taken to ensure the output
        format of this method matches the Java DOT graph output method we have defined so that we can simply
        compare the actual and expected outputs by comparing the strings.
        """
        output = f'digraph "{self.name}" {{\n'
        def key(node: Node) -> int: return node.processor_index

        # We have to sort by the key as groupby just iterates through the list and creates a new group whenever the key changes
        for _, processor_nodes in groupby(sorted(self.nodes, key=key), key):
            sorted_processor_nodes = sorted(processor_nodes, key=lambda node: node.start_time)

            for node in sorted_processor_nodes:
                outgoing_edges = self.find_outgoing_edges(node)
                output += node.to_output_dot_node() + '\n'

                if (len(outgoing_edges) > 0):
                    output += '\n'.join([edge.to_dot_edge() for edge in self.find_outgoing_edges(node)]) + '\n'
        output += '}'

        return output

    def __str__(self) -> str:
        """
        Returns a string representation of this graph for debugging purposes.
        """
        stringified_nodes = ', '.join([str(node) for node in self.nodes])
        stringified_edges = ', '.join([str(edge) for edge in self.edges])

        return f'Graph[name={self.name}, nodes={stringified_nodes}, edges={stringified_edges}]'


def parse_attributes(element: ET.Element) -> Dict[str, any]:
    """
    Parses all the gxl attributes for the given element into a dictionary. The values within this dictionary will
    be parsed to their defined types. These attributes should be stored in the following format within the gxl file:

    ```xml
    <attr name="KEY">
        <int>VALUE</int>
    </attr>
    ```

    Elements may have multiple attributes, but each attribute must have exactly one child element which is either an
    int or a string.
    """

    attribute_dict = {}

    for attribute in element.findall('attr'):
        key = attribute.get('name')
        children = list(attribute)
        if (len(children) != 1):
            raise SyntaxError(
                f'Expected <attr name="{key}"> to have exactly one child element')

        value = children[0]
        if (value.tag == 'int'):
            attribute_dict[key] = int(value.text)
        elif (value.tag == 'string'):
            attribute_dict[key] = value.text
        else:
            # This should never happen, but if it does we want to know about it
            raise SyntaxError(f'Unexpected tag <{value.tag}> in <attr name="{key}">')

    return attribute_dict
