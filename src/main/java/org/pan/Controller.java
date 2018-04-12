package org.pan;

import com.sun.javafx.stage.StageHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jssc.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private final Bytes bytes = new Bytes();
    public ComboBox<String> comList;
    public TextArea message;
    private SerialPort serialPort;
    private final int rightLength = 494;
    private byte[] fingerTmp;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String[] portNames = SerialPortList.getPortNames();
        if (portNames.length > 0) {
            comList.getItems().addAll(portNames);
            comList.getSelectionModel().select(portNames[0]);
        }
    }

    public void outputFinger(ActionEvent actionEvent) {
        if (this.fingerTmp == null) {
            showMessage(Alert.AlertType.ERROR,"请先选择一个指纹模板");
            return;
        }


        if (this.fingerTmp.length != rightLength) {
            showMessage(Alert.AlertType.ERROR,"指纹模板长度有误，正确的长应是" + rightLength);
            return;
        }

        Runnable runnable = new Runnable() {
            public void run() {
                for (int i = 0; i * 150 < fingerTmp.length; i++) {
                    try {
                        byte[] header = Bytes.string2bytes("AA 32 41 A1 00 01 01 01 00");
                        byte[] bytes = Arrays.copyOfRange(fingerTmp, i*150, Math.min((i+1) * 150, fingerTmp.length));
                        byte[] end = Bytes.string2bytes("00 BB");

                        ByteBuffer allocate = ByteBuffer.allocate(header.length + bytes.length + end.length);
                        allocate.put(header).put(bytes).put(end);
                        byte[] array = allocate.array();
                        serialPort.writeBytes(array);
                        printLog("发送：" + Bytes.bytes2string(array));

                        byte[] readBytes = serialPort.readBytes(11, 3000);
                        printLog("接收：" + Bytes.bytes2string(readBytes));
                    } catch (SerialPortException e) {
                        printLog("下载指纹模板失败");
                    } catch (SerialPortTimeoutException e) {
                        printLog("下载指纹模板超时");
                        break;
                    }
                }
            }
        };

        new Thread(runnable).start();
    }

    public void inputFinger(ActionEvent actionEvent) {
        Stage stage = StageHelper.getStages().get(0);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("请选择一个指纹模板");
        File file = fileChooser.showOpenDialog(stage);
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(file.toURI()));
            String s = new String(bytes, "UTF-8").trim();
            this.fingerTmp = Bytes.string2bytes(s.trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(ActionEvent actionEvent) {
        if (serialPort == null || !serialPort.isOpened()) {
            printLog("串口未打开");
        }else{
            try {
                serialPort.closePort();
                printLog("串口"+serialPort.getPortName()+"关闭成功");
            } catch (SerialPortException e) {
                printLog("串口"+serialPort.getPortName()+"关闭失败");
            }
        }
    }

    public void open(ActionEvent actionEvent){
        Object selectedItem = comList.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showMessage(Alert.AlertType.ERROR, "请选择一个端口");
            return;
        }

        String com = (String) selectedItem;

        if (serialPort != null && serialPort.isOpened()) {
            printLog("串口" + com + "己打开");
            return;
        }

        serialPort = new SerialPort(com);
        try {
            serialPort.openPort();
            printLog("串口" + com + "打开成功");
        } catch (SerialPortException e) {
            printLog("串口" + com + "打开失败，可能被占用");
        }
    }

    private void printLog(String text) {
        message.appendText(LocalTime.now().toString() + ":\t" + text);
        message.appendText("\r\n");
    }

    private void showMessage(Alert.AlertType type, String text) {
        Alert alert = new Alert(type);
        alert.setHeaderText(text);
        alert.showAndWait();
    }

}
