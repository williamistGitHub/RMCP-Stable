package org.mcphackers.mcp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.mcphackers.mcp.tasks.Task;
import org.mcphackers.mcp.tasks.Task.Side;
import org.mcphackers.mcp.tasks.mode.TaskMode;
import org.mcphackers.mcp.tasks.mode.TaskParameter;

public abstract class MCP {

	public static final String NAME = "RMCP-Stable";
	public static final String VERSION = "r1";
	public static final String REPO = "williamistGitHub/RMCP-Stable";
	public static final String GITHUB_URL = "https://github.com/" + REPO;

	protected MCP() {
		Update.attemptToDeleteUpdateJar();
	}

	public abstract Path getWorkingDir();
	
	public final boolean performTask(TaskMode mode, Side side) {
		return performTask(mode, side, true);
	}

	public final boolean performTask(TaskMode mode, Side side, boolean completionMsg) {
		List<Task> tasks = mode.getTasks(this);
		if(tasks.size() == 0) {
			System.err.println("Performing 0 tasks");
			return false;
		}
		
		boolean enableProgressBars = mode.usesProgressBars;
		
		List<Task> performedTasks = new ArrayList<>();
		for (Task task : tasks) {
			if (task.side == side || task.side == Side.ANY) {
				performedTasks.add(task);
			}
			else if (side == Side.ANY) {
				if (task.side == Side.SERVER || task.side == Side.CLIENT) {
					if(mode.requirement.get(this, task.side)) {
						performedTasks.add(task);
					}
				}
			}
		}
		if(enableProgressBars) setProgressBars(performedTasks, mode);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		setActive(false);

		AtomicInteger result1 = new AtomicInteger(Task.INFO);

		for(int i = 0; i < performedTasks.size(); i++) {
			Task task = performedTasks.get(i);
			final int barIndex = i;
			if(enableProgressBars) {
				task.setProgressBarIndex(barIndex);
			}
			pool.execute(() -> {
				try {
					task.performTask();
				} catch (Exception e) {
					result1.set(Task.ERROR);
					e.printStackTrace();
				}
				if(enableProgressBars) {
					setProgress(barIndex, "Finished!", 100);
				}
			});
		}
		
		pool.shutdown();
		while (!pool.isTerminated()) {}

		byte result = result1.byteValue();
		
		List<String> msgs = new ArrayList<>();
		for(Task task : performedTasks) {
			msgs.addAll(task.getMessageList());
			byte retresult = task.getResult();
			if(retresult > result) {
				result = retresult;
			}
		}
		//TODO display this info in the pop up message (Maybe)
		if(msgs.size() > 0) log("");
		for(String msg : msgs) {
			log(msg);
		}

		if(completionMsg) {
			String[] msgs2 = {"Finished successfully!", "Finished with warnings!", "Finished with errors!"};
			showMessage(mode.getFullName(), msgs2[result], result);
		}
		setActive(true);
		if(enableProgressBars) clearProgressBars();
		return result != Task.ERROR;
	}

	public abstract void setProgressBars(List<Task> tasks, TaskMode mode);

	public abstract void clearProgressBars();

	public abstract void log(String msg);

	public abstract Options getOptions();

	public abstract String getCurrentVersion();

	public abstract void setCurrentVersion(String version);

	public abstract void setProgress(int barIndex, String progressMessage);

	public abstract void setProgress(int barIndex, int progress);

	public abstract void setActive(boolean active);

	public abstract boolean yesNoInput(String title, String msg);

	public abstract String inputString(String title, String msg);

	public abstract void showMessage(String title, String msg, int type);

	public abstract boolean updateDialogue(String changelog, String version);

	public void setProgress(int barIndex, String progressMessage, int progress) {
		setProgress(barIndex, progress);
		setProgress(barIndex, progressMessage);
	}

	public void setParameter(TaskParameter param, Object value) throws IllegalArgumentException {
		getOptions().setParameter(param, value);
	}

	public void safeSetParameter(TaskParameter param, String value) {
		if(value != null) {
			if(getOptions().safeSetParameter(param, value)) return;
			showMessage(param.desc, "Invalid value!", Task.ERROR);
		}
	}
}
