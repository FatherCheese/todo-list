package cookie.todo.client.gui;

import cookie.todo.TodoClient;
import cookie.todo.client.Page;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.TextFieldElement;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class ScreenTodo extends Screen {
	int xSize = 176;
	int ySize = 166;
	private TextFieldElement textField;
	private Page currentPage;
	private int selectedList;

	@Override
	public void init() {
		try {
			TodoClient.read();
		} catch (IOException e) {
			TodoClient.LOGGER.error("Error reading todo.dat for the current world or server!");
		}

		Keyboard.enableRepeatEvents(true);
		buttons.clear();
		textField = new TextFieldElement(this, font,(width - xSize) / 2 - 10, (height - ySize) / 2 - 24, 200, 20, "", null);
		textField.setMaxStringLength(20);
		currentPage = TodoClient.getPage(0);
		selectedList = 0;
	}

	@Override
	public void render(int mx, int my, float partialTick) {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		renderBackground();
		mc.textureManager.loadTexture("/assets/todo/gui/todo.png").bind();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		// Checkmarks
		for (int check = 0; check < 6; check++) {
			drawTexturedModalRect(x + 16, y + 16 + (18 * check), 176, 0, 16, 16);
		}
		if (currentPage.isFirstChecked()) {
			drawTexturedModalRect(x + 16, y + 16, 192, 0, 16, 16);
		}
		if (currentPage.isSecondChecked()) {
			drawTexturedModalRect(x + 16, y + 34, 192, 0, 16, 16);
		}
		if (currentPage.isThirdChecked()) {
			drawTexturedModalRect(x + 16, y + 52, 192, 0, 16, 16);
		}
		if (currentPage.isFourthChecked()) {
			drawTexturedModalRect(x + 16, y + 70, 192, 0, 16, 16);
		}
		if (currentPage.isFifthChecked()) {
			drawTexturedModalRect(x + 16, y + 88, 192, 0, 16, 16);
		}
		if (currentPage.isSixthChecked()) {
			drawTexturedModalRect(x + 16, y + 106, 192, 0, 16, 16);
		}

		// Page Buttons
		drawTexturedModalRect(x + 32, y + 128, 208, 0, 16, 16);
		drawTexturedModalRect(x + 128, y + 128, 224, 0, 16, 16);

		// Page Text
		font.drawStringWithShadow("Page " + currentPage.id, x + 70, y + 130, -1);

		for (int loop = 0; loop < 6; loop++) {
			font.drawString(currentPage.getLine(loop), x + 34, y + 22 + (18 * loop), 0x99876c);
		}

		if (textField.isFocused) {
			textField.drawTextBox();
		}
	}

	@Override
	public void mouseClicked(int mx, int my, int buttonNum) {
		if (!textField.isFocused) {
			super.mouseClicked(mx, my, buttonNum);
			int x = (width - xSize) / 2;
			int y = (height - ySize) / 2;

			for (int i = 0; i < 6; i++) {
				if (mx >= x + 32 && mx < x + 160) {
					if (my >= y + 16 + (18 * i) && my < y + 32 + (18 * i)) {
						textField.setFocused(true);
					}
				}
			}

			mouseClickedSlot(mx, my);
			mouseClickedBox(mx, my);

			// Page Back
			if (mx >= x + 32 && mx < x + 48) {
				if (my >= y + 128 && my < y + 144) {
					if (currentPage.id - 1 >= 0) {
						currentPage = TodoClient.getPage(currentPage.id - 1);
					}
				}
			}

			// Page forward
			if (mx >= x + 128 && mx < x + 144) {
				if (my >= y + 128 && my < y + 144) {
					currentPage = TodoClient.getPage(currentPage.id + 1);
				}
			}
		}
	}

	private void mouseClickedBox(int mx, int my) {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;

		if (mx >= x + 16 && mx < x + 32) {
			if (my >= y + 16 && my < y + 34) {
				if (!currentPage.isFirstChecked()) {
					currentPage.setFirstChecked(true);
				} else {
					currentPage.setFirstLine("");
					currentPage.setFirstChecked(false);
				}
			} else if (my >= y + 34 && my < y + 52) {
				if (!currentPage.isSecondChecked()) {
					currentPage.setSecondChecked(true);
				} else {
					currentPage.setSecondLine("");
					currentPage.setSecondChecked(false);
				}
			} else if (my >= y + 52 && my < y + 70) {
				if (!currentPage.isThirdChecked()) {
					currentPage.setThirdChecked(true);
				} else {
					currentPage.setThirdLine("");
					currentPage.setThirdChecked(false);
				}
			} else if (my >= y + 70 && my < y + 88) {
				if (!currentPage.isFourthChecked()) {
					currentPage.setFourthChecked(true);
				} else {
					currentPage.setFourthLine("");
					currentPage.setFourthChecked(false);
				}
			} else if (my >= y + 88 && my < y + 106) {
				if (!currentPage.isFifthChecked()) {
					currentPage.setFifthChecked(true);
				} else {
					currentPage.setFifthLine("");
					currentPage.setFifthChecked(false);
				}
			} else if (my >= y + 106 && my < y + 124) {
				if (!currentPage.isSixthChecked()) {
					currentPage.setSixthChecked(true);
				} else {
					currentPage.setSixthLine("");
					currentPage.setSixthChecked(false);
				}
			}
		}
	}

	private void mouseClickedSlot(int mx, int my) {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;

		if (mx >= x + 32 && mx < x + 160) {
			if (my >= y + 16 && my < y + 34) {
				selectedList = 0;
			} else if (my >= y + 34 && my < y + 52) {
				selectedList = 1;
			} else if (my >= y + 52 && my < y + 70) {
				selectedList = 2;
			} else if (my >= y + 70 && my < y + 88) {
				selectedList = 3;
			} else if (my >= y + 88 && my < y + 106) {
				selectedList = 4;
			} else if (my >= y + 106 && my < y + 124) {
				selectedList = 5;
			}
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void keyPressed(char eventCharacter, int eventKey, int mx, int my) {
		if (textField.isFocused) {
			textField.textboxKeyTyped(eventCharacter, eventKey);

			if(eventKey == Keyboard.KEY_RETURN) {
				currentPage.setLine(selectedList, textField.getText());
				textField.setFocused(false);
				textField.setText("");
			}

			if (eventKey == Keyboard.KEY_ESCAPE) {
				textField.setFocused(false);
				textField.setText("");
			}
		} else {
			super.keyPressed(eventCharacter, eventKey, mx, my);
		}
	}

	@Override
	public void removed() {
		try {
			TodoClient.write();
		} catch (IOException e) {
			TodoClient.LOGGER.error("Error writing todo.dat for the current world or server!");
			TodoClient.LOGGER.error(e.getLocalizedMessage(), e);
		}

		TodoClient.pages.clear();
		Keyboard.enableRepeatEvents(false);
	}
}
