from typing import List
from GxlToDot import Graph, Node
import sys
import os


SCRIPT_PATH = os.path.dirname(os.path.realpath(__file__))
ROOT_PATH = os.path.abspath(os.path.join(SCRIPT_PATH, '..'))


def create_unit_test_file(graphs: List[Graph], output_path: str) -> None:
    """
    Generates a Java class file in the specified output path directory of the project with unit tests that
    verify the optimal schedulers we have designed for each of the given graphs.
    """
    class_name = 'OptimalSchedulerTest'
    test_file_content = f"""package nz.ac.auckland.se306.group12;

import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This class has been automatically generated by the GenerateUnitTests.py script.
 * Do not modify this file directly as it will be overwritten the next time the script is run.
 * <p>
 * The optimal schedules for each test have been sourced from 
 * <a href="https://parallel.auckland.ac.nz/OptimalTaskScheduling/OptimalSchedules.html">the amazing work</a>
 * done by the Parallel and Reconfigurable Computing Lab at the University of Auckland.
 */
public class {class_name} {{
    """

    filename = os.path.join(output_path, class_name + '.java')
    # Open file in write mode to overwrite any existing content
    with open (filename, 'w') as f:
        f.write(test_file_content)

    with open (filename, 'a') as f:
        for graph in graphs:
            f.write(create_unit_test(graph))

        f.write('}\n')


def create_unit_test(graph: Graph) -> str:
    """
    Creates a string representing a single Java unit test for the given graph.
    """
    scheduled_tasks_array = ', '.join([to_scheduled_task(node) for node in graph.nodes])
    processor_end_times_array = ', '.join(str(end_time) for end_time in graph.get_processor_end_times())
    method_name = f'testOptimal{to_valid_method_name(graph)}'

    return f"""
    @ParameterizedTest
    @MethodSource("nz.ac.auckland.se306.group12.TestUtil#getOptimalSchedulers")
    void {method_name}(Scheduler scheduler) {{
        Graph graph = TestUtil.loadGraph("./graphs/optimal/{graph.get_filename()}");
        int processorCount = {graph.processor_count};
        int expectedScheduleEndTime = {graph.optimal_schedule_end_time};

        Schedule actualSchedule = scheduler.schedule(graph, processorCount);

        Assertions.assertEquals(expectedScheduleEndTime, actualSchedule.getEndTime());

        ScheduledTask[] expectedScheduledTasks = new ScheduledTask[]{{{scheduled_tasks_array}}};
        int[] expectedProcessorEndTimes = new int[]{{{processor_end_times_array}}};

        Schedule expectedSchedule = new Schedule(expectedScheduledTasks, expectedProcessorEndTimes, {len(graph.nodes)});
        
        Assertions.assertEquals(expectedSchedule, actualSchedule);
    }}
    
"""


def to_valid_method_name(graph: Graph) -> str:
    """
    Converts the name of the graph into a valid Java method name by replacing any invalid characters with
    suitable replacements.
    """
    return graph.name.replace('.', 'dot').replace('-', '_').replace('#', '')


def to_scheduled_task(node: Node) -> str: 
    """
    Converts the given node into a string representing a new ScheduledTask object in Java.
    """
    return f'new ScheduledTask({node.start_time}, {node.finish_time}, {node.processor_index})'


def get_gxl_file_paths(path: str, limit = -1) -> List[str]:
    """
    Returns a list of all the file paths for the GXL files in the given directory. If a limit is specified,
    only up to that many file paths will be returned.
    """
    filenames = [os.path.join(path, filename) for filename in os.listdir(path) if filename.endswith('.gxl')]
    return filenames if limit <= 0 else filenames[:limit]


def generate_graphs(input_path: str, input_dot_graph_path: str, test_path: str, graph_limit: int) -> None:
    """
    Generates DOT files from the GXL files found in the given input path directory and outputs them at the input
    DOT graph path directory. The graph limit parameter specifies the maximum number of graphs it will retrieve from the
    input path. It also generates a Java class file with unit tests for each of the graphs in the test path director.
    """
    gxl_file_paths = get_gxl_file_paths(input_path, graph_limit)
    print(f'Found {len(gxl_file_paths)} gxl files in "{prettify_path(input_path)}"')

    graphs: List[Graph] = []
    for index, filename in enumerate(gxl_file_paths):
        graph = Graph(filename)
        write_input_dot_graph(graph, input_dot_graph_path, index + 1)
        graphs.append(graph)

    create_unit_test_file(graphs, test_path)


def write_input_dot_graph(graph: Graph, output_dir: str, position: int) -> None:
    """
    Writes the input DOT graph to the given output directory with the filename specified by the id of the
    GXL graph.
    """
    output_path = os.path.join(output_dir, graph.get_filename())

    with open(output_path, 'w') as f:
        print(f'{position} - Writing input DOT graph to "{prettify_path(output_path)}"')
        f.write(graph.to_input_dot_graph())


def prettify_path(path: str) -> str:
    """
    Returns a prettified version of the given path by not showing the root directory. This is used to make 
    the output of the script more readable.
    """
    return os.path.abspath(path).replace(ROOT_PATH, '<root>')


def main():
    """
    The entry point of this script. It converts GXL representations of task graphs and converts them into unit
    tests that can be run to verify the correctness of the optimal schedulers we have designed. It expects up 
    to two arguments:

    1. The input path of the GXL files to generate the unit tests from. By default, this is the current directory.
    2. The maximum number of GXL files to generate unit tests for. By default this is 50. Specifying -1 will generate
       unit tests for all GXL files in the input directory.
    """
    # By default, use the current directory
    input_path = sys.argv[1] if len(sys.argv) >= 2 else '.'
    graph_limit = int(sys.argv[2]) if len(sys.argv) >= 3 else 50

    input_dot_graph_path = os.path.join(ROOT_PATH, 'graphs', 'optimal')
    test_path = os.path.join(ROOT_PATH, 'src', 'test', 'java', 'nz', 'ac', 'auckland', 'se306', 'group12')

    if (not os.path.isdir(input_path)):
        raise FileNotFoundError(f'Expected input path "{prettify_path(input_path)}" to be a valid directory')
    if (not os.path.isdir(input_dot_graph_path)):
        raise FileNotFoundError(f'Expected the directory "{prettify_path(input_dot_graph_path)}" to exist')
    if (not os.path.isdir(test_path)):
        raise FileNotFoundError(f'Expected the directory "{prettify_path(test_path)}" to exist')

    generate_graphs(input_path, input_dot_graph_path, test_path, graph_limit)


if __name__ == '__main__':
    main()
