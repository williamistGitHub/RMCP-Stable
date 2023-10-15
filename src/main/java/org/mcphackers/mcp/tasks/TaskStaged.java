package org.mcphackers.mcp.tasks;

import org.mcphackers.mcp.MCP;

public abstract class TaskStaged extends Task {
	
	public int step;
	public Stage[] stages;
	
	public TaskStaged(Side side, MCP instance, ProgressListener listener) {
		super(side, instance, listener);
	}

	public TaskStaged(Side side, MCP instance) {
		super(side, instance);
	}
	
	public TaskStaged(MCP instance) {
		super(instance);
	}
	
	protected final void step() {
		step++;
	}
	
	protected abstract Stage[] setStages();
	
	public void doTask() throws Exception {
		stages = setStages();
		while(step < stages.length) {
			setProgress(stages[step].stageName, stages[step].completion);
			stages[step].doTask();
			step();
		}
	}
	
	public void overrideStage(int stageIndex, TaskRunnable task) {
		if(stageIndex < stages.length) {
			stages[stageIndex].setOperation(task);
		}
	}
	
	public Stage stage(String name, int percentage, TaskRunnable task) {
		return new Stage(name, percentage, task);
	}
	
	public Stage stage(String name, TaskRunnable task) {
		return new Stage(name, -1, task);
	}
	
	public class Stage {
		private TaskRunnable runnable;
		private String stageName;
		private int completion;
		
		public Stage(String name, int i, TaskRunnable task) {
			setOperation(task);
			stageName = name;
			completion = i;
		}
		
		private void setOperation(TaskRunnable task) {
			runnable = task;
		}
		
		private void doTask() throws Exception {
			runnable.doTask();
		}
	}

}
