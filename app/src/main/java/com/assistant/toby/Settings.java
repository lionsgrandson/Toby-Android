package com.assistant.toby;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Display;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;

public class Settings {
    public Settings() {

    }

    public void writeToFile(String data, Context context) {
        try {
            File file = new File(context.getFilesDir(), "Settings.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
//            FileOutputStream outputStreamWriter = new FileOutputStream(file.getName(), false);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file.toString(),false));

            bw.newLine();
            bw.write(data);
            bw.flush();
            bw.close();

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public String readFromFile(Context context) {
        String longOrShort = "";

        try {

            File file = new File(context.getFilesDir(), "Settings.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            String contents="";
            BufferedReader br= new BufferedReader(new FileReader(file));
            while(br.readLine()!=null) {
                contents = String.valueOf(br.readLine());
                if (contents.contains("short")) {
                    longOrShort = "false";
                } else if (contents.contains("false")) {
                    longOrShort = "true";
                }
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return longOrShort;
    }
}
