package editor;

import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.geometry.VPos;

public class Node {
	public Text entry;
	public Node back;
	public Node next;

	public Node(Text e, Node n) {
		entry = e;
		next = n;
	}

	public Text getEntry() {
		return entry;
	}
}