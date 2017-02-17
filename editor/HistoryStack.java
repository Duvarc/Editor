package editor;

import java.util.LinkedList;

public class HistoryStack {

	private LinkedList<Node> undoStack;
	private LinkedList<Node> redoStack;

	public HistoryStack() {
		undoStack = new LinkedList<Node>();
		redoStack = new LinkedList<Node>();
	}

	public HistoryStack(Node n) {
		undoStack = new LinkedList<Node>();
		redoStack = new LinkedList<Node>();
		put(n);
	}

	public LinkedList<Node> getUndoStack() {
		return undoStack;
	}

	public LinkedList<Node> getRedoStack() {
		return redoStack;
	}

	public void put(Node x) {
		undoStack.push(x);
		if (undoStack.size() > 100) {
			undoStack.removeLast();
		}
	}
	public Node undo() {
		Node past = undoStack.pop();
		redoStack.push(past);
		return past;
	}

	public Node redo() {
		Node future = redoStack.pop();
		undoStack.push(future);
		return future;
	}

	public void clearRedoStack() {
		redoStack.clear();
	}
}