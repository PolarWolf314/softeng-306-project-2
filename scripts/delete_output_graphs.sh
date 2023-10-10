#!/usr/bin/env bash
if [[ $PWD == *"project-2-team-12" ]]; then
	for graph in graphs/*-output.dot; do
		rm $graph
		echo -e "\e[1;33mDeleted\e[m ${graph}"
	done
else
	echo -e "\e[33;4mdelete_output_graphs.sh\e[24m must be run from project root\e[m"
	echo -e "cd to root, then run \e[1mscripts/delete_output_graphs.sh\e[m"
fi
