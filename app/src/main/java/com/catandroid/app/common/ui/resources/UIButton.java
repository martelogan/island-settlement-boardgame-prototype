package com.catandroid.app.common.ui.resources;

public class UIButton {

	public enum ButtonType {
		PLAYER_STATUS, VIEW_BARBARIANS, TRADE, DICE_ROLL, BUILD_ROAD, BUILD_SHIP, MOVE_SHIP,
		BUILD_SETTLEMENT,BUILD_CITY, BUILD_CITY_WALL, PLAY_PROGRESS_CARD, HIRE_KNIGHT,
		ACTIVATE_KNIGHT, PROMOTE_KNIGHT, CHASE_ROBBER, CHASE_PIRATE, MOVE_KNIGHT,
		PURCHASE_CITY_IMPROVEMENT, USE_FISH, CANCEL_ACTION, END_TURN;
	}
	
	public enum ButtonBackground {
		DEFAULT, PRESSED, ACTIVATED
	}

	//testing push
	private int x, y, width, height;
	private boolean pressed, enabled;
	private ButtonType buttonType;

	public UIButton(ButtonType buttonType, int width, int height) {
		this.buttonType = buttonType;
		this.x = 0;
		this.y = 0;
		this.width = width;
		this.height = height;

		pressed = false;
		enabled = false;
	}

	public boolean press(int x, int y) {
		if (!enabled)
		{
			return false;
		}

		pressed = isWithin(x, y);
		return pressed;
	}

	public boolean release(int x, int y) {
		if (!pressed || !enabled)
		{
			return false;
		}

		pressed = false;
		return isWithin(x, y);
	}

	public boolean isWithin(int x, int y) {
		x += width / 2;
		y += height / 2;
		return (x > this.x && x < this.x + width && y > this.y && y < this.y
				+ height);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isPressed() {
		return pressed;
	}

	public ButtonType getButtonType() {
		return buttonType;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
