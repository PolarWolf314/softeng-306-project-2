from typing import List
from GxlToDot import Graph, Node
import sys
import os

SCRIPT_PATH = os.path.dirname(os.path.realpath(__file__))
ROOT_PATH = os.path.abspath(os.path.join(SCRIPT_PATH, '..'))

def create_unit_test_file(graphs: List[Graph], output_path: str) -> None:
    class_name = 'OpimalSchedulerTest'
    test_file_content = f"""package nz.ac.auckland.se306.group12.optimal;

import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class {class_name} {{
    """

    filename = os.path.join(output_path, class_name + '.java')
    # Open file in write mode to overwrite any existing content
    with open (filename, 'w') as f:
        f.write(test_file_content)

    with open (filename, 'a') as f:
        for graph in graphs:
            f.write(create_unit_test(graph))

        f.write('}')

def create_unit_test(graph: Graph) -> str:
    scheduled_tasks_array = ', '.join([to_scheduled_task(node) for node in graph.nodes])
    processor_end_times_array = ', '.join(str(end_time) for end_time in graph.get_processor_end_times())

    return f"""
    @Test
    void testOptimal{graph.name}() {{
        Graph graph = TestUtil.loadGraph("./graphs/optimal/{graph.get_filename()}");
        int processorCount = {graph.processor_count};
        
        ScheduledTask[] expectedScheduledTasks = new []{{{scheduled_tasks_array}}};
        int[] expectedProcessorEndTimes = new []{{{processor_end_times_array}}};

        Schedule expectedSchedule = new Schedule(expectedScheduledTasks, expectedProcessorEndTimes, {len(graph.nodes)});
        Schedule actualSchedule = null; // TODO: Create a scheduler

        Assertions.assertEqual(expectedSchedule, actualSchedule);
    }}
    """

def to_scheduled_task(node: Node) -> str: 
    return f'new ScheduledTask({node.start_time}, {node.finish_time}, {node.processor_index})'

def get_gxl_file_paths(path: str) -> List[str]:
    """
    Returns a list of all the file paths for the gxl files in the given directory.
    """
    return [os.path.join(path, filename) for filename in os.listdir(path) if filename.endswith('.gxl')]


def generate_graphs(input_path: str, input_dot_graph_path: str, test_path: str) -> None:
    gxl_file_paths = get_gxl_file_paths(input_path)
    print(f'Found {len(gxl_file_paths)} gxl files in "{prettify_path(input_path)}"')

    graphs: List[Graph] = []
    for filename in gxl_file_paths:
        graph = Graph(filename)
        write_input_dot_graph(graph, input_dot_graph_path)
        graphs.append(graph)

    create_unit_test_file(graphs, test_path)

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

    input_dot_graph_path = os.path.join(ROOT_PATH, 'graphs', 'optimal')
    test_path = os.path.join(ROOT_PATH, 'src', 'test', 'java', 'nz', 'ac', 'auckland', 'se306', 'group12')

    if (not os.path.isdir(input_path)):
        raise FileNotFoundError(f'Expected input path "{prettify_path(input_path)}" to be a valid directory')
    if (not os.path.isdir(input_dot_graph_path)):
        raise FileNotFoundError(f'Expected the directory "{prettify_path(input_dot_graph_path)}" to exist')
    if (not os.path.isdir(test_path)):
        raise FileNotFoundError(f'Expected the directory "{prettify_path(test_path)}" to exist')

    generate_graphs(input_path, input_dot_graph_path, test_path)

if __name__ == '__main__':
    main()