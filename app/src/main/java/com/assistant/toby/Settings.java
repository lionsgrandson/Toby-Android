package com.assistant.toby;

import android.content.Context;
import android.util.Log;
import android.view.Display;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Settings {
    public Settings() {

    }

    public void writeToFile(String data, Context context) {
        try {
            FileOutputStream  outputStreamWriter = new FileOutputStream("Settings.txt",false);
            outputStreamWriter.write( data.getBytes());
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public boolean readFromFile(Context context) {
         boolean longOrShort = true;
        ArrayList ret = new ArrayList();
        try {
            InputStream inputStream = context.openFileInput("Settings.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                    if(receiveString.equalsIgnoreCase("short")){
                        longOrShort = false;
                    }else if(receiveString.equalsIgnoreCase("long")){
                        longOrShort = true;
                    }
                }

                inputStream.close();
                ret.add(stringBuilder.toString());

            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return longOrShort;
    }
}
