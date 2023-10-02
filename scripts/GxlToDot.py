from typing import List, Dict
from itertools import groupby
import xml.etree.ElementTree as ET

class Node:
    def __init__(self, node: ET.Element):
        self.id = node.get('id')

        attributes = parse_attributes(node)
        self.start_time = attributes["Start time"]
        self.weight = attributes["Weight"]
        self.finish_time = attributes["Finish time"]
        self.processor = attributes["Processor"]

    def to_input_dot_node(self) -> str:
        return f'{self.id} [Weight={self.weight}];'

    def to_output_dot_node(self) -> str:
        # +1 on the processor as we want to start at 1 instead of 0
        return f'{self.id} [Weight={self.weight},Start={self.start_time},Processor={self.processor + 1}];'

    def __str__(self) -> str:
        return f'Node[id={self.id}, start_time={self.start_time}, weight={self.weight}, finish_time={self.finish_time}, processor={self.processor}]'


class Edge:
    def __init__(self, edge: ET.Element):
        self.source = edge.get('from')
        self.target = edge.get('to')

        attributes = parse_attributes(edge)
        self.weight = attributes["Weight"]

    def to_dot_edge(self) -> str:
        return f'{self.source} -> {self.target} [Weight={self.weight}];'

    def __str__(self) -> str:
        return f'Edge[source={self.source}, target={self.target}, weight={self.weight}]'


class Graph:
    def __init__(self, filename: str):
        tree = ET.parse(filename)
        root = tree.getroot()
        graph = root.find('graph')

        self.name: str = graph.get('id')
        self.nodes: List[Node] = []
        self.edges: List[Edge] = []

        for nodeTag in graph.findall('node'):
            node = Node(nodeTag)
            self.nodes.append(node)

        for edgeTag in graph.findall('edge'):
            edge = Edge(edgeTag)
            self.edges.append(edge)

        # Determine the number of unique processors in this graph
        self.processor_count = len(set([node.processor for node in self.nodes]))

    def get_filename(self) -> str:
        return self.name + '.dot'

    def find_outgoing_edges(self, node: Node) -> List[Edge]:
        """
        Returns a list of all edges that have the given node as their source.
        """
        return [edge for edge in self.edges if edge.source == node.id]

    def to_input_dot_graph(self) -> str:
        output = 'digraph ' + f'"{self.name}"' + ' {\n'
        output += '\n'.join([node.to_input_dot_node() for node in self.nodes]) + '\n'
        output += '\n'.join([edge.to_dot_edge() for edge in self.edges]) + '\n'
        output += '}'

        return output

    def to_output_dot_graph(self) -> str:
        """
        Builds a string of the dot graph representation of this graph. Care has been taken to ensure the output
        format of this method matches the Java dot graph output method we have defined so that we can simply
        compare the actual and expected outputs by comparing the strings.
        """
        output = 'digraph ' + f'"{self.name}"' + ' {\n'
        def key(node: Node) -> int: return node.processor

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
        stringifiedNodes = ', '.join([str(node) for node in self.nodes])
        stringifiedEdges = ', '.join([str(edge) for edge in self.edges])

        return f'Graph[name={self.name}, nodes={stringifiedNodes}, edges={stringifiedEdges}]'


def parse_attributes(element: ET.Element) -> Dict[str, any]:
    """
    Parses all the gxl attributes for the given element into a dictionary. The values within this dictionary will
    be parsed to their defined types. These attributes should be stored in the following format within the gxl file:

    <attr name="KEY">
        <int>VALUE</int>
    </attr>

    Elements may have multiple attributes, but each attribute must have exactly one child element which is either an
    int or a string.
    """

    attributeDict = {}

    for attribute in element.findall('attr'):
        key = attribute.get('name')
        children = list(attribute)
        if (len(children) != 1):
            raise SyntaxError(
                f'Expected <attr name="{key}"> to have exactly one child element')

        value = children[0]
        if (value.tag == 'int'):
            attributeDict[key] = int(value.text)
        elif (value.tag == 'string'):
            attributeDict[key] = value.text
        else:
            # This should never happen, but if it does we want to know about it
            raise SyntaxError(f'Unexpected tag <{value.tag}> in <attr name="{key}">')

    return attributeDict
