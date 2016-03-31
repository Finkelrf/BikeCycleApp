package com.example.finkel.bikecycle;

/**
 * Created by Finkel on 08/03/2016.
 */
public class BluetoothCommunicator {


    private enum BT_CMDS{
        SET_DISPLAY,
        PWM,
        RIGHT,//turn right
        LEFT,//turn left
        RDB1,//take the 1st exit in the round about
        RDB2,//take the 2nd exit in the round about
        RDB3,//take the 3th exit in the round about
        RDB4,//take the 4th exit in the round about
        STRAIGHT,//keep going straight
        BACK, //turn around
        DEMO,
    }
    private final char END_CMD_CHAR = ':';
    private final char FINAL_BYTE = 'p';
    public BluetoothCommunicator() {

    }

    public boolean sendLedRingPwm(int pwm,BluetoothManager bm){
        if(pwm>100 || pwm < 0) {
            return false;
        }else {
            if(bm != null) {
                if (bm.isConnected()) {
                    bm.sendMsg(BT_CMDS.PWM.toString() + END_CMD_CHAR + pwm + FINAL_BYTE);
                    LiveLogger.setLog("cmd send " + BT_CMDS.PWM.toString() + END_CMD_CHAR + pwm + FINAL_BYTE);
                    return true;
                } else {
                    LiveLogger.setLog("bt not connected");
                    return false;
                }
            }else{
                LiveLogger.setLog("bt not connected");
                return false;
            }
        }
    }

    public boolean sendDisplay(String info, BluetoothManager bm){
        if(bm != null) {
            if (bm.isConnected()) {
                bm.sendMsg(BT_CMDS.SET_DISPLAY.toString() + END_CMD_CHAR + info + FINAL_BYTE);
                LiveLogger.setLog("cmd send " + BT_CMDS.SET_DISPLAY.toString() + END_CMD_CHAR + info + FINAL_BYTE);
                return true;
            } else {
                LiveLogger.setLog("bt not connected");
                return false;
            }
        }else{
            LiveLogger.setLog("bt not connected");
            return false;
        }
    }

    public boolean startDemo(BluetoothManager bm) {
        String info = "99";
        if(bm != null) {
            if (bm.isConnected()) {
                bm.sendMsg(BT_CMDS.DEMO.toString() + END_CMD_CHAR + info + FINAL_BYTE);
                LiveLogger.setLog("cmd send " + BT_CMDS.DEMO.toString() + END_CMD_CHAR + info + FINAL_BYTE);
                return true;
            } else {
                LiveLogger.setLog("bt not connected");
                return false;
            }
        }else{
            LiveLogger.setLog("bt not connected");
            return false;
        }
    }

    public boolean sendDirection(String direction, double distanceToNextTurn, BluetoothManager bm) {
        if(bm != null) {
            if (bm.isConnected()) {
                bm.sendMsg(direction + END_CMD_CHAR + distanceToNextTurn + FINAL_BYTE);
                LiveLogger.setLog("cmd send " + direction + END_CMD_CHAR + distanceToNextTurn + FINAL_BYTE);
                return true;
            } else {
                LiveLogger.setLog("bt not connected");
                return false;
            }
        }else{
            LiveLogger.setLog("bt not connected");
            return false;
        }
    }
}
