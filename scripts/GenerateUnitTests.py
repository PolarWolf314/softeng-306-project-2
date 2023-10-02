from typing import List
from GxlToDot import Graph
import sys
import os

SCRIPT_PATH = os.path.dirname(os.path.realpath(__file__))
ROOT_PATH = os.path.abspath(os.path.join(SCRIPT_PATH, '..'))

def create_unit_test(graph: Graph) -> str: 
    return f"""
    @Test
    void testOptimal{graph.name}() {{
        Graph graph = TestUtil.loadGraph("./graphs/optimal/{graph.get_filename()}");
        String expectedOutput = "{graph.to_output_dot_graph()}";
    }}
    """

def get_gxl_file_paths(path: str) -> List[str]:
    """
    Returns a list of all the file paths for the gxl files in the given directory.
    """
    return [os.path.join(path, filename) for filename in os.listdir(path) if filename.endswith('.gxl')]


def generate_graphs(input_path: str, input_dot_graph_path: str) -> None:
    gxl_file_paths = get_gxl_file_paths(input_path)
    print(f'Found {len(gxl_file_paths)} gxl files in "{prettify_path(input_path)}"')

    for filename in gxl_file_paths:
        graph = Graph(filename)
        write_input_dot_graph(graph, input_dot_graph_path)


def write_input_dot_graph(graph: Graph, output_dir: str) -> None:
    """
    Writes the input dot graph to the given output directory with the filename specified by the id of the
    gxl graph.
    """
    output_path = os.path.join(output_dir, graph.get_filename())

    with open(output_path, 'w') as f:
        print(f'Writing input dot graph to "{prettify_path(output_path)}"')
        f.write(graph.to_input_dot_graph())

def prettify_path(path: str) -> str:
    """
    Returns a prettified version of the given path by not showing the root directory. This is used to make 
    the output of the script more readable.
    """
    return os.path.abspath(path).replace(ROOT_PATH, '<root>')

def main():
    # By default, use the current directory
    input_path = sys.argv[1] if len(sys.argv) >= 2 else '.'
    # output_path = sys.argv[2] if len(sys.argv) >= 3 else '../src/test/java/nz/ac/auckland/se306/group12/optimal'

    input_dot_graph_path = os.path.join(ROOT_PATH, 'graphs', 'optimal')
    test_path = os.path.join(ROOT_PATH, 'src', 'test', 'java', 'nz', 'ac', 'auckland', 'se306', 'group12', 'optimal')

    if (not os.path.isdir(input_path)):
        raise FileNotFoundError(f'Expected input path "{prettify_path(input_path)}" to be a valid directory')
    if (not os.path.isdir(input_dot_graph_path)):
        raise FileNotFoundError(f'Expected the directory "{prettify_path(input_dot_graph_path)}" to exist')
    if (not os.path.isdir(test_path)):
        raise FileNotFoundError(f'Expected the directory "{prettify_path(test_path)}" to exist')

    generate_graphs(input_path, input_dot_graph_path)

if __name__ == '__main__':
    main()