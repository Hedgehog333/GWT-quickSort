package com.gwt.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.gwt.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

import java.util.ArrayList;
import java.util.Random;

public class Alieiev implements EntryPoint {
	static Button sendButton = new Button("Enter");
	static TextBox nameField = new TextBox();

	static ArrayList<Integer> randomList;

	static InsertFormHandler handler = new InsertFormHandler();
	static CellsHandler cellsHandler = new CellsHandler();

	static boolean isReset;

	private static final String INCORRECT_RANGE_ALERT_MESSAGE = "Invalid value! Correct value from 1 until 1000";
	private static final String TABLE_COUNT_CLICK_ALERT_MESSAGE = "Please select a value smaller or equal to 30.";

	public void onModuleLoad() {
		sendButton.addStyleName("sendButton");

		RootPanel.get("numberFieldContainer").add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButton);

		nameField.setFocus(true);
		nameField.selectAll();

		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
	}

	static class CellsHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			int cellValue = Integer.parseInt(event.getRelativeElement().getInnerText());
			if (cellValue > 30) {
				Window.alert(TABLE_COUNT_CLICK_ALERT_MESSAGE);
				return;
			}

			QuickSort.setRepeatSort(true);
			randomList = RandomIntegerArray.generateArrayWithOneLessOrEqualsThen(cellValue, 30);
			htmlGenerator.genTable(ListConverter.convertToButtonList(randomList));
		}
	}

	static class InsertFormHandler implements ClickHandler, KeyUpHandler {
		public void onClick(ClickEvent event) {
			sendCountToServer();
		}

		public void onKeyUp(KeyUpEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				sendCountToServer();
			}
		}

		private void sendCountToServer() {
			String countNumbers = nameField.getText();
			if (!FieldVerifier.isValidCount(countNumbers)) {
				Window.alert(INCORRECT_RANGE_ALERT_MESSAGE);
				return;
			}

			RootPanel.get("insertForm").setVisible(false);
			isReset = false;

			randomList = RandomIntegerArray.generateArrayWithOneLessOrEqualsThen(Integer.parseInt(countNumbers), 30);
			htmlGenerator.genTable(ListConverter.convertToButtonList(randomList));
			htmlGenerator.genButtons();
		}
	}

	static class htmlGenerator {
		public static void genTable(ArrayList<Button> buttonsList) {
			int countColumns = (int) Math.ceil(buttonsList.size() / 10.0);
			FlexTable flexTable = new FlexTable();
			flexTable.getElement().setId("table");

			for (int i = 0, cell = 0; i < countColumns; i++) {
				for (int j = 0; j < 10; j++, cell++) {
					if ( i == countColumns-1 && cell >= buttonsList.size())
						break;

					Button itemButton = buttonsList.get(cell);
					itemButton.addClickHandler(cellsHandler);
					flexTable.setWidget(j, i, itemButton);
				}
			}

			clearTable();
			RootPanel.get("tableWrapperContainer").add(flexTable);
		}

		public static void clearTable() {
			RootPanel.get("tableWrapperContainer").clear();
		}

		public static void genButtons(){
			final Button resetButton = new Button("Reset");
			final Button startButton = new Button("Start");

			RootPanel.get("resetButtonContainer").add(resetButton);
			RootPanel.get("startSortButtonContainer").add(startButton);

			resetButton.addClickHandler(event -> {
				isReset = true;
				clearButtons();
				clearTable();
				RootPanel.get("insertForm").setVisible(true);
				nameField.setText("");
				nameField.setFocus(true);
				QuickSort.setRepeatSort(true);
			});

			startButton.addClickHandler(event -> {
				isReset = false;
				QuickSort.changeSortOrder();
				QuickSort.sort(randomList, 0, randomList.size()-1);
			});
		}

		public static void clearButtons(){
			RootPanel.get("resetButtonContainer").clear();
			RootPanel.get("startSortButtonContainer").clear();
		}
	}

	static class QuickSort {
		private static boolean repeatSort = true;
		static final int delayMillis = 100;

		public static void changeSortOrder() {
			repeatSort = !repeatSort;
		}

		public static void setRepeatSort(boolean repeatSort) {
			QuickSort.repeatSort = repeatSort;
		}

		public static int partition(ArrayList<Integer> arr, int low, int high) {
			int pivot = arr.get(high);
			int i = (low-1);
			for (int j=low; j<high; j++) {
				if (repeatSort ? arr.get(j) > pivot : arr.get(j) < pivot) {
					i++;

					int temp = arr.get(i);
					arr.set(i, arr.get(j));
					arr.set(j, temp);
				}
			}

			int temp = arr.get(i + 1);
			arr.set(i + 1, arr.get(high));
			arr.set(high, temp);

			return i+1;
		}

		public static void sort(ArrayList<Integer> arr, int low, int high) {
			if (low < high) {
				int pi = partition(arr, low, high);
				if(!isReset)
					htmlGenerator.genTable(ListConverter.convertToButtonList(randomList));
				else return;

				Timer timer = new Timer() {
					@Override
					public void run() {
						sort(arr, low, pi - 1);

						Timer timer = new Timer() {
							@Override
							public void run() {
								sort(arr, pi + 1, high);
							}
						};

						timer.schedule(delayMillis);
					}
				};

				timer.schedule(delayMillis);
			}
		}
	}

	public static class RandomIntegerArray {
		public static int getRandomNumber() {
			int min = 1, max = 1000;
			Random random = new Random();
			return random.nextInt(max - min) + min;
		}

		public static ArrayList<Integer> generateArray(Integer size) {
			ArrayList<Integer> localArray = new ArrayList<>();

			for (int i = 0; i < size; i++) {
				localArray.add(getRandomNumber());
			}

			return localArray;
		}

		public static ArrayList<Integer> generateArrayWithOneLessOrEqualsThen(Integer size, Integer lessOrEqualsThen) {
			ArrayList<Integer> randomList = generateArray(size);
			while (randomList.stream().min(Integer::min).get() >= lessOrEqualsThen) {
				randomList = generateArray(size);
			}
			return randomList;
		}
	}

	public static class ListConverter {
		public static ArrayList<Button> convertToButtonList(ArrayList<Integer> list) {
			ArrayList<Button> buttonsList = new ArrayList<>();
			for (int i = 0; i < list.size(); i++ ) {
				Button itemButton = new Button(list.get(i).toString());
				itemButton.setStyleName("cell");
				buttonsList.add(itemButton);
			}

			return buttonsList;
		}
	}
}