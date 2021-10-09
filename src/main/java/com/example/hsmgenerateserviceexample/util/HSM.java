package com.example.hsmgenerateserviceexample.util;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;

public class HSM {
    private String mhsmIP;
    private int mhsmPort;
    private String mpvk;
    private String mPVK;
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public HSM(String hsmIP, int hsmPort, String pvk, String PVK) {
        this.mhsmIP = hsmIP;
        this.mhsmPort = hsmPort;
        this.mpvk = pvk;
        this.mPVK = PVK;
    }

    public String getEncPINZPK(String sKey, String cardNo, String EncPINDataBlock) {
        String encPINBlock = EncPINDataBlock.substring(EncPINDataBlock.length() - 16);
        String eKeyP = this.getEKeyP(EncPINDataBlock.substring(0, EncPINDataBlock.length() - 16));
        if (eKeyP.length() < 32) {
            return eKeyP.substring(0, 2) + eKeyP + "1";
        } else {
            String ret = this.getEncPIN(eKeyP, sKey, encPINBlock, cardNo.substring(cardNo.length() - 13, cardNo.length() - 1)).substring(6);
            return ret.length() < 12 ? ret + ret + "BK2" : ret;
        }
    }

    public String getNewPINOffset(String cardNo, String NewPINDataBlock) {
        String encPINBlock = NewPINDataBlock.substring(NewPINDataBlock.length() - 16);
        String eKeyP = this.getEKeyP(NewPINDataBlock.substring(0, NewPINDataBlock.length() - 16));
        if (eKeyP.length() < 32) {
            return eKeyP.substring(0, 2) + eKeyP + "1";
        } else {
            String ret = this.getEncPIN(eKeyP, encPINBlock, cardNo.substring(cardNo.length() - 13, cardNo.length() - 1)).substring(6);
            return ret.length() < 12 ? ret + ret + "BK2" : ret;
        }
    }

    public String getChangePINOffset(String cardNo, String ChangePINDataBlock, String sPINOffset) {
        String encPINBlockNew = ChangePINDataBlock.substring(ChangePINDataBlock.length() - 16);
        String encPINBlock = ChangePINDataBlock.substring(ChangePINDataBlock.length() - 32, ChangePINDataBlock.length() - 16);
        String eKeyP = this.getEKeyP(ChangePINDataBlock.substring(0, ChangePINDataBlock.length() - 32));
        if (eKeyP.length() < 32) {
            return eKeyP.substring(0, 2) + eKeyP + "1";
        } else {
            String ret = this.getNewPIN(eKeyP, encPINBlock, cardNo.substring(cardNo.length() - 13, cardNo.length() - 1), sPINOffset, encPINBlockNew).substring(6);
            return ret.length() < 12 ? ret + ret + "DU2" : ret;
        }
    }

    public String verifyPIN(String cardNo, String ATMPINDataBlock, String sPINOffset) {
        String encPINBlock = ATMPINDataBlock.substring(ATMPINDataBlock.length() - 16);
        String eKeyP = this.getEKeyP(ATMPINDataBlock.substring(0, ATMPINDataBlock.length() - 16));
        return this.verifyPIN(eKeyP, encPINBlock, cardNo.substring(cardNo.length() - 13, cardNo.length() - 1), sPINOffset).substring(6);
    }

    public String getHSMKey(String sHKey) {
        String msg = "0008GI010130000256";
        BigInteger hk = new BigInteger(sHKey, 16);
        BigInteger pk = new BigInteger(this.mpvk, 16);
        byte[] b = hk.toByteArray();
        byte[] bID = new byte[256];
        if (b[0] == 0) {
            for(int i = 1; i < b.length; ++i) {
                bID[i - 1] = b[i];
            }
        } else {
            bID = hk.toByteArray();
        }

        msg = this.getHSMResponse(msg, bID, ";99", pk.toByteArray(), ";0U0");
        return msg.substring(18, 51);
    }

    public String encMsg(String seKey, String seMsg) {
        return this.encryptMsgHSM(seKey, seMsg).substring(12);
    }

    public String decMsg(String seKey, String seMsg) {
        return convertHexToString(this.decryptMsgHSM(seKey, seMsg).substring(12));
    }

    private String decryptMsgHSM(String sEKey, String seMsg) {
        String msg = "0005M2001100A";
        msg = msg + sEKey;
        String ss = Integer.toHexString(seMsg.length()).toUpperCase();

        for(int i = ss.length(); i < 4; ++i) {
            ss = "0" + ss;
        }

        msg = msg + ss;
        msg = msg + seMsg;
        return this.getHSMResponse(msg);
    }

    private String encryptMsgHSM(String sEKey, String sMsg) {
        String msg = "0005M0001100A";
        msg = msg + sEKey;
        sMsg = bytesToHex(sMsg.getBytes());
        int i;
        if (sMsg.length() % 16 > 0) {
            for(i = sMsg.length() % 16; i < 16; ++i) {
                sMsg = sMsg + "0";
            }
        }

        String ss = Integer.toHexString(sMsg.length()).toUpperCase();

        for(i = ss.length(); i < 4; ++i) {
            ss = "0" + ss;
        }

        msg = msg + ss;
        msg = msg + sMsg;
        return this.getHSMResponse(msg);
    }

    private static String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        for(int i = 0; i < hex.length() - 1; i += 2) {
            String output = hex.substring(i, i + 2);
            int decimal = Integer.parseInt(output, 16);
            if (decimal > 0) {
                sb.append((char)decimal);
                temp.append(decimal);
            }
        }

        return sb.toString();
    }

    private static byte hexToInt(char a) {
        switch(a) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            default:
                return 0;
            case 'A':
                return 10;
            case 'B':
                return 11;
            case 'C':
                return 12;
            case 'D':
                return 13;
            case 'E':
                return 14;
            case 'F':
                return 15;
        }
    }

    private static byte toByte(int number) {
        int tmp = number & 255;
        return (tmp & 128) == 0 ? (byte)tmp : (byte)(tmp - 256);
    }

    private static byte hexToByte(char a, char b) {
        return toByte(hexToInt(a) * 16 + hexToInt(b));
    }

    private static byte[] hexToBytes(String sKey) {
        byte[] res = new byte[sKey.length() / 2];

        for(int i = 0; i < res.length; ++i) {
            res[i] = hexToByte(sKey.charAt(i * 2), sKey.charAt(i * 2 + 1));
        }

        return res;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for(int j = 0; j < bytes.length; ++j) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 15];
        }

        return new String(hexChars);
    }

    private String getEncPIN(String sEKey, String sKey, String sEPIN, String sPAN) {
        String msg = "0008CA";
        msg = this.getHSMResponse(msg + sEKey + sKey + "06" + sEPIN + "0101" + sPAN);
        return msg;
    }

    private String getEncPIN(String sEKey, String sEPIN, String sPAN) {
        String msg = "0008BK002";
        msg = this.getHSMResponse(msg + sEKey + this.mPVK + sEPIN + "0106" + sPAN + "12345678901234561234567890N6");
        return msg;
    }

    private String verifyPIN(String sEKey, String sEPIN, String sPAN, String sPINOffset) {
        String msg = "0008DA";
        msg = this.getHSMResponse(msg + sEKey + this.mPVK + "12" + sEPIN + "0106" + sPAN + "12345678901234561234567890N6" + sPINOffset);
        return msg;
    }

    private String getNewPIN(String sEKey, String sEPIN, String sPAN, String sPINOffset, String sENPIN) {
        String msg = "0008DU002";
        msg = this.getHSMResponse(msg + sEKey + this.mPVK + sEPIN + "0106" + sPAN + "12345678901234561234567890N6" + sPINOffset + sENPIN);
        return msg;
    }

    private String getEKey(String sHKey) {
        String msg = "0008GI010116000256";
        BigInteger hk = new BigInteger(sHKey, 16);
        BigInteger pk = new BigInteger(this.mpvk, 16);
        byte[] b = hk.toByteArray();
        byte[] bID = new byte[64];
        if (b[0] == 0) {
            for(int i = 1; i < b.length; ++i) {
                bID[i - 1] = b[i];
            }
        } else {
            bID = hk.toByteArray();
        }

        msg = this.getHSMResponse(msg, bID, ";99", pk.toByteArray(), ";0U0");
        return msg.substring(18, 51);
    }

    private String getEKeyE(String sHKey) {
        String msg = "0008GI010130000064";
        BigInteger hk = new BigInteger(sHKey, 16);
        BigInteger pk = new BigInteger(this.mpvk, 16);
        byte[] b = hk.toByteArray();
        byte[] bID = new byte[64];
        if (b[0] == 0) {
            for(int i = 1; i < b.length; ++i) {
                bID[i - 1] = b[i];
            }
        } else {
            bID = hk.toByteArray();
        }

        msg = this.getHSMResponse(msg, bID, ";99", pk.toByteArray(), ";0U0");
        return msg.substring(18, 51);
    }

    private String getEKeyP(String sHKey) {
        String msg = "0008GI010114000256";
        BigInteger hk = new BigInteger(sHKey, 16);
        BigInteger pk = new BigInteger(this.mpvk, 16);
        byte[] b = hk.toByteArray();
        byte[] bID = new byte[256];
        if (b[0] == 0) {
            for(int i = 1; i < b.length; ++i) {
                bID[i - 1] = b[i];
            }
        } else {
            bID = hk.toByteArray();
        }

        msg = this.getHSMResponse(msg, bID, ";99", pk.toByteArray(), ";0U0");
        return msg.length() < 50 ? msg + "GI" : msg.substring(18, 51);
    }

    private static int convert(int n) {
        return Integer.valueOf(String.valueOf(n), 16);
    }

    private String getMAC(String sEKey, String sMsg) {
        String msg = "0004M602030003";
        msg = msg + sEKey;
        msg = msg + "00" + Integer.toHexString(sMsg.length());
        msg = msg + sMsg;
        System.out.println(sMsg);
        return this.getHSMResponse(msg);
    }

    private String getVerifyMAC(String sEKey, String sMsg, String sMac) {
        String msg = "0005M802030003";
        if (sMsg.length() % 24 > 0) {
            for(int i = sMsg.length() % 24; i < 24; ++i) {
                sMsg = sMsg + "|";
            }
        }

        msg = msg + sEKey;
        msg = msg + "00" + Integer.toHexString(sMsg.length());
        msg = msg + sMsg;
        msg = msg + sMac;
        System.out.println(sMsg);
        return this.getHSMResponse(msg);
    }

    private String getVerifyPIN(String sEKeyP, String sAccount, String sPINLMK, String sEPIN) {
        String msg = "0005BC" + sEKeyP + sEPIN + "03" + sAccount + sPINLMK;
        return this.getHSMResponse(msg);
    }

    private String getHSMEncrypt(String data, byte[] bData, String sMsg) {
        String response = "";

        try {
            Socket hsmClient = new Socket(this.mhsmIP, this.mhsmPort);
            DataOutputStream output = new DataOutputStream(hsmClient.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(hsmClient.getInputStream()));
            byte[] sData = new byte[data.length() + 2];
            byte[] aData = data.getBytes();
            int ld = data.length() + bData.length + sMsg.length();
            sData[0] = (byte)(ld / 256);
            sData[1] = (byte)(ld % 256);

            int i;
            for(i = 0; i < aData.length; ++i) {
                sData[i + 2] = aData[i];
            }

            System.out.print(data);
            System.out.print(bytesToHex(bData));
            System.out.println(sMsg);
            output.write(sData);
            output.write(bData);
            output.write(sMsg.getBytes());
            output.flush();
            int len = reader.read() * 256;
            len += reader.read();

            for(i = 0; i < len; ++i) {
                response = response + (char)reader.read();
            }

            reader.close();
            output.close();
            hsmClient.close();
            return response.substring(6);
        } catch (IOException var14) {
            return "99" + this.mhsmIP + this.mhsmPort + var14.toString();
        }
    }

    private String getHSMResponse(String data, byte[] sHKey, String sMid, byte[] sPKey, String sFoot) {
        String response = "";

        try {
            Socket hsmClient = new Socket(this.mhsmIP, this.mhsmPort);
            DataOutputStream output = new DataOutputStream(hsmClient.getOutputStream());
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(hsmClient.getInputStream()));
            byte[] sData = new byte[data.length() + 2];
            byte[] aData = data.getBytes();
            int ld = data.length() + sHKey.length + sMid.length() + sPKey.length + sFoot.length();
            sData[0] = (byte)(ld / 256);
            sData[1] = (byte)(ld % 256);

            int i;
            for(i = 0; i < aData.length; ++i) {
                sData[i + 2] = aData[i];
            }

            output.write(sData);
            output.write(sHKey);
            output.write(sMid.getBytes());
            output.write(sPKey);
            output.write(sFoot.getBytes());
            output.flush();
            int len = inputStream.read() * 256;
            len += inputStream.read();

            for(i = 0; i < len; ++i) {
                response = response + (char)inputStream.read();
            }

            inputStream.close();
            output.close();
            hsmClient.close();
            return response.substring(6);
        } catch (IOException var16) {
            return "99" + this.mhsmIP + this.mhsmPort + var16.toString();
        }
    }

    private String getHSMResponse(String data) {
        String response = "";

        try {
            Socket hsmClient = new Socket(this.mhsmIP, this.mhsmPort);
            DataOutputStream output = new DataOutputStream(hsmClient.getOutputStream());
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(hsmClient.getInputStream()));
            byte[] sData = new byte[data.length() + 2];
            byte[] aData = data.getBytes();
            int ld = data.length();
            sData[0] = (byte)(ld / 256);
            sData[1] = (byte)(ld % 256);

            int i;
            for(i = 0; i < aData.length; ++i) {
                sData[i + 2] = aData[i];
            }

            output.write(sData);
            output.flush();
            int len = inputStream.read();
            len += inputStream.read();

            for(i = 0; i < len; ++i) {
                response = response + (char)inputStream.read();
            }

            inputStream.close();
            output.close();
            hsmClient.close();
            return response;
        } catch (IOException var12) {
            return "99" + this.mhsmIP + this.mhsmPort + var12.toString();
        }
    }

    private static String leadZero(int v, int n) {
        String ret = "" + v;

        for(int i = ret.length(); i < n; ++i) {
            ret = "0" + ret;
        }

        return ret;
    }
}
