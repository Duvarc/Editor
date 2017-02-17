package editor;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.Iterator;
import javafx.stage.Stage;

public class FastLinkedList implements Iterable<Node>{


	private Node sentinel;
	private int size;
	public Node currentNode;
	public int currentPos;

	public class ListIterator implements Iterator<Node> {
		private Node current;
		private int pos;

		public ListIterator() {
			current = sentinel;
			pos = 0;
		}

		public boolean hasNext() {
			return (pos < size);
		}

		public Node next() {
			current = current.next;
			pos++;
			return current;
		}
	}

	public Iterator<Node> iterator() {
		return new ListIterator();
	}

	public FastLinkedList() {
		currentPos = 0;
		size = 0;
		sentinel = new Node(new Text(5, 0, null ), null);
		sentinel.next = sentinel;
		sentinel.back = sentinel;
		currentNode = sentinel;
	}

	public Node getFirst() {
		return sentinel.next;
	}

	public Text getFirstText() {
		return sentinel.next.entry;
	}

	public Node getLast() {
		return sentinel.back;
	}

	public Text getLastText() {
		return sentinel.back.entry;
	}

	private Text getBack() {
		return sentinel.back.entry;
	}

	public Node getCurrentNode() {
		return currentNode;
	}

	public void addLast(Text x) {
		Node nLast = new Node(x, sentinel);
		sentinel.back.next = nLast;
		nLast.back = sentinel.back;
		sentinel.back = nLast;
		size++;
	}

	public Node addCurrent(Text x) {
		Node newText = new Node(x, currentNode.next);
		currentNode.next.back = newText;
		currentNode.next = newText;
		newText.back = currentNode;
		currentNode = currentNode.next;
		size++;
		currentPos++;
		return newText;
	}

	public Node addCurrentNode(Node x) {
		x.next = currentNode.next;
		currentNode.next.back = x;
		currentNode.next = x;
		x.back = currentNode;
		currentNode = x;
		size++;
		currentPos++;
		return x;
	}

	public Node deleteCurrent() {
		if (currentNode != sentinel) {
			Node oldNode = currentNode;
			currentNode.back.next = currentNode.next;
			currentNode.next.back = currentNode.back;
			currentNode = currentNode.back;
			size--;
			currentPos--;
			return oldNode;
		}
		return sentinel;
	}

	public boolean isEmpty() {
		if (size == 0) {
			return true;
		}
		return false;
	}

	public int size() {
		return size;
	}

	public Text removeFirst() {

		if (isEmpty()) {
			return null;
		}

		Text oldFirst = getFirst().entry;
		sentinel.next = sentinel.next.next;
		sentinel.next.back = sentinel;
		size--;
		return oldFirst;
	}

	public Text removeLast() {

		if (isEmpty()) {
			return null;
		}

		Text oldLast = sentinel.back.entry;

		sentinel.back = sentinel.back.back;
		sentinel.back.next = sentinel;
		size--;
		return oldLast;
	}

	public Text get(int index) {
		int i = 0;
		Node temp = getFirst();
		if (index >= size) {
			return null;
		}
		while (i < index) {
			temp = temp.next;
			i++;
		}
		return temp.entry;
	}

	public int getCurrentNodeX() {
		return Math.round((float) currentNode.entry.getX());
	}

	public int getCurrentNodeY() {
		return Math.round((float) currentNode.entry.getY());
	}

	public void goLeft() {
		if (currentNode != sentinel) {
			currentNode = currentNode.back;
		}
	}

	public void goRight() {
		if (currentNode.next != sentinel) {
			currentNode = currentNode.next;
		}
	}

	public void goUp() {
		Node x1 = currentNode;
		Node x2 = currentNode;

		double cursorX = x1.getEntry().getX() + x1.getEntry().getLayoutBounds().getWidth();
		double cursorPoint = cursorX - x1.getEntry().getLayoutBounds().getWidth();
		double cursorY = x1.getEntry().getY();
		int pastLine = Math.round((float) (cursorY - currentNode.getEntry().getLayoutBounds().getHeight()));

		if (pastLine < 0) {
			pastLine = 0;
		}

		boolean firstLine = ((int) x1.getEntry().getY() == 0);

		if (firstLine) {
			currentNode = sentinel;
		}

		else {
			while (x1.getEntry().getY() == cursorY) {
				x1 = x1.back;
			}
			
			while (x1.getEntry().getX() > cursorX) {
				x1 = x1.back;
			}
			x1 = x1.back;
			x2 = x1.next;



			boolean sameLine = (x1.getEntry().getY() == pastLine);
			boolean sameLine2 = (x2.getEntry().getY() == pastLine);

			/*System.out.println("-------------");
			System.out.println(pastLine);
			System.out.println(x1.getEntry().getText());
			System.out.println(x2.getEntry().getText());
			System.out.println(x1.getEntry().getX());
			System.out.println(x2.getEntry().getX());*/
			

			double firstChoice = x1.getEntry().getX();
			double secondChoice = x2.getEntry().getX();

			if (Math.abs(cursorPoint - firstChoice) <= Math.abs(cursorPoint - secondChoice) && sameLine) {
				if (cursorX > x1.getEntry().getX() + x1.getEntry().getLayoutBounds().getWidth()) {
					currentNode = x2;
				}
				else {
					currentNode = x1;
				}
			}
			else if (Math.abs(cursorPoint - firstChoice) <= Math.abs(cursorPoint - secondChoice) && !sameLine && !sameLine2) {
				if (x2.getEntry().getText() == "\n") {
					currentNode = x2;
				}
				else {
					currentNode = x1;
				}
			}

			else {
				currentNode = x2;
			}
		}
	}

	public void goDown() {
		Node x1 = currentNode;
		Node x2 = currentNode;

		double cursorX = x1.getEntry().getX();
		double cursorPoint = x1.getEntry().getLayoutBounds().getWidth() + cursorX;
		double cursorY = x1.getEntry().getY();

		boolean lastLine = false;

		while (x1.getEntry().getY() == cursorY) {
			x1 = x1.next;
			if (x1 == sentinel) {
				lastLine = true;
				cursorY += 1;
			}
		}

		if (lastLine) {
			currentNode = sentinel.back;
		}
		else {


			double nextLineY = x1.getEntry().getY();
			while (x1.getEntry().getX() <= cursorX && x1.getEntry().getY() == nextLineY) {
				x1 = x1.next;
			}

			x2 = x1.back;

			if (cursorPoint <= 5) {
				if (x2.getEntry().getText() == "\n") {
					currentNode = x2;
				}
				else {
					currentNode = x2.back;
				}
			}
			else if (Math.abs(cursorX - x1.getEntry().getX()) < Math.abs(cursorX - x2.getEntry().getX())) {
				currentNode = x1;
			}
			else {
				currentNode = x2;
			}
		}
	}

	public void setCurrentNode(Node x) {
		currentNode = x;
	}

	public void jumpToStart() {
		currentNode = sentinel;
	}

}