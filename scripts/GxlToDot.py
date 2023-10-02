from typing import List, Dict
import xml.etree.ElementTree as ET

class Graph:
    def __init__(self, filename: str):
        tree = ET.parse(filename)
        root = tree.getroot()
        graph = root.find('graph')

        self.name = graph.get('id')
        self.nodes = []
        self.edges = []

        for nodeTag in graph.findall('node'):
            node = Node(nodeTag)
            self.nodes.append(node)

        for edgeTag in graph.findall('edge'):
            edge = Edge(edgeTag)
            self.edges.append(edge)
    
    def __str__(self) -> str:
        stringifiedNodes = ', '.join([str(node) for node in self.nodes])
        stringifiedEdges = ', '.join([str(edge) for edge in self.edges])

        return f'Graph[name={self.name}, nodes={stringifiedNodes}, edges={stringifiedEdges}]'
    
class Node:
    def __init__(self, node: ET.Element):
        self.id = node.get('id')
    
        attributes = parse_attributes(node)
        self.start_time = attributes["Start time"]
        self.weight = attributes["Weight"]
        self.finish_time = attributes["Finish time"]
        self.processor = attributes["Processor"]

    def __str__(self) -> str:
        return f'Node[id={self.id}, start_time={self.start_time}, weight={self.weight}, finish_time={self.finish_time}, processor={self.processor}]'

class Edge:
    def __init__(self, edge: ET.Element):
        self.source = edge.get('from')
        self.target = edge.get('to')

        attributes = parse_attributes(edge)
        self.weight = attributes["Weight"]

    def __str__(self) -> str:
        return f'Edge[source={self.source}, target={self.target}, weight={self.weight}]'
    
def parse_attributes(element: ET.Element) -> Dict[str, any]:
    attributeDict = {}

    for attribute in element.findall('attr'):
        key = attribute.get('name')
        children = list(attribute)
        if (len(children) != 1):
            raise SyntaxError(f'Expected <attr name="{key}"> to have exactly one child element')
        
        value = children[0]
        if (value.tag == 'int'):
            attributeDict[key] = int(value.text)
        elif (value.tag == 'string'):
            attributeDict[key] = value.text
        else:
            # This should never happen, but if it does we want to know about it
            raise SyntaxError(f'Unexpected tag <{value.tag}> in <attr name="{key}">')
    
    return attributeDict
    
def main():
    graph = Graph('Fork_Join_Nodes_10_CCR_0.10_WeightType_Random_Homogeneous-2.gxl')
    print(graph)

if __name__ == '__main__':
    main()