package cookie.todo.client;

import java.util.List;

public class Page {
	public static final int MASK_CHECKED_ONE = 0b0000_0001;
	public static final int MASK_CHECKED_TWO = 0b0000_0010;
	public static final int MASK_CHECKED_THR = 0b0000_0100;
	public static final int MASK_CHECKED_FOR = 0b0000_1000;
	public static final int MASK_CHECKED_FIV = 0b0001_0000;
	public static final int MASK_CHECKED_SIX = 0b0010_0000;
	public byte checkboxes;
	public List<String> lines;
	public int id;

	public Page(int id, byte checkboxes, List<String> lines) {
		this.id = id;
		this.checkboxes = checkboxes;
		this.lines = lines;
	}

	public Page(byte checkboxes, List<String> lines) {
		this.lines = lines;
		this.checkboxes = checkboxes;
	}

	public void setCheckbox(boolean flag, int mask) {
		if (flag) {
			checkboxes = (byte) (checkboxes | mask);
		} else {
			checkboxes = (byte) (checkboxes & ~mask);
		}
	}
	public void setLine(int i, String s) {
		lines.set(i, s);
	}

	public boolean isFirstChecked() {
		return (checkboxes & MASK_CHECKED_ONE) != 0;
	}
	public boolean isSecondChecked() {
		return (checkboxes & MASK_CHECKED_TWO) != 0;
	}
	public boolean isThirdChecked() {
		return (checkboxes & MASK_CHECKED_THR) != 0;
	}
	public boolean isFourthChecked() {
		return (checkboxes & MASK_CHECKED_FOR) != 0;
	}
	public boolean isFifthChecked() {
		return (checkboxes & MASK_CHECKED_FIV) != 0;
	}
	public boolean isSixthChecked() {
		return (checkboxes & MASK_CHECKED_SIX) != 0;
	}
	public void setFirstChecked(boolean flag) {
		setCheckbox(flag, MASK_CHECKED_ONE);
	}
	public void setSecondChecked(boolean flag) {
		setCheckbox(flag, MASK_CHECKED_TWO);
	}
	public void setThirdChecked(boolean flag) {
		setCheckbox(flag, MASK_CHECKED_THR);
	}
	public void setFourthChecked(boolean flag) {
		setCheckbox(flag, MASK_CHECKED_FOR);
	}
	public void setFifthChecked(boolean flag) {
		setCheckbox(flag, MASK_CHECKED_FIV);
	}
	public void setSixthChecked(boolean flag) {
		setCheckbox(flag, MASK_CHECKED_SIX);
	}

	public String getLine(int i) {
		return lines.get(i);
	}
	public void setFirstLine(String s) {
		setLine(0, s);
	}
	public void setSecondLine(String s) {
		setLine(1, s);
	}
	public void setThirdLine(String s) {
		setLine(2, s);
	}
	public void setFourthLine(String s) {
		setLine(3, s);
	}
	public void setFifthLine(String s) {
		setLine(4, s);
	}
	public void setSixthLine(String s) {
		setLine(5, s);
	}
}
