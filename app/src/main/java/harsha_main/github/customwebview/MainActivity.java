package harsha_main.github.customwebview;


import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends Activity {
    final String Url = "https://drive.google.com/uc?export=download&id=1oPmin1dmKGsEzCRNiZ_o2Bbg7uicKhU7";
    String base = Environment.getExternalStorageDirectory().toString();
    String filename = "/Webfiles.zip";
    String directory = "/newdir";
    String StorezipFileLocation = base + directory + filename;
    WebView webView;
    ProgressBar progressBar;
    Button but;
    ProcessZipfile mew;
    String device_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.web);
        but = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);

        device_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        new File(base + directory).mkdirs();
        mew = new ProcessZipfile();
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mew.execute(Url);
                progressBar.setVisibility(View.VISIBLE);
                but.setVisibility(View.GONE);
                Animator animator = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    animator = ViewAnimationUtils.createCircularReveal(getWindow().getDecorView(), getWindow().getDecorView().getWidth() / 2, getWindow().getDecorView().getHeight() / 2, 0, 500);
                    animator.start();
                }
                Toast.makeText(MainActivity.this, "getting data", Toast.LENGTH_SHORT).show();
            }
        });

    }

    protected void onResume() {
        super.onResume();
        if (isNetworkConnected()) {
            return;
        }
        Toast.makeText(this, "Check your internet and try again", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need internet connection for this app. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect to Internet").setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Open the settings
                        startActivity(new Intent("android.settings.SETTINGS"));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //close activity in case of no connection
                        MainActivity.this.finish();
                    }
                });
        builder.create().show();
    }

    private boolean isNetworkConnected() {
        return ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    class ProcessZipfile extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... aurl) {
            int count;

            try {
                URL url = new URL(aurl[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream input = new BufferedInputStream(url.openStream());

                OutputStream output = new FileOutputStream(StorezipFileLocation);

                byte data[] = new byte[1024];

                while ((count = input.read(data)) != -1) {

                    output.write(data, 0, count);
                }
                output.close();
                input.close();

                //download finished
                extractZip();

                modifyhtml();

            } catch (Exception e) {
            }
            return null;
        }


        void extractZip() throws Exception {
            //start decompressing
            int BUFFER_SIZE = 4096;
            String zipFilePath = base + directory + filename;
            String destDirectory = base + directory;
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
            while (true) {
                ZipEntry entry = zipIn.getNextEntry();
                if (entry == null) break;
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extract it
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                    byte[] bytesIn = new byte[BUFFER_SIZE];
                    int read = 0;
                    while ((read = zipIn.read(bytesIn)) != -1) {
                        bos.write(bytesIn, 0, read);
                    }
                    bos.close();
                } else {
                    //else, create the directory
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
            }
            zipIn.close();
        }

        void modifyhtml() throws Exception {
            //create a new html file which displays Device Id

            String content = "document.write(\"Device id: " + device_id + "\")";

            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(base + directory + "/First-Aid Kit_files/device.js")));
            writer.write(content);
            writer.close();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this, "Finished", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl("file:///sdcard" + directory + "/First-Aid Kit.html");
            Animator animator = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                animator = ViewAnimationUtils.createCircularReveal(getWindow().getDecorView(), getWindow().getDecorView().getWidth() / 2, getWindow().getDecorView().getHeight() / 2, 500, 0);
                animator.start();
            }
        }
    }
}