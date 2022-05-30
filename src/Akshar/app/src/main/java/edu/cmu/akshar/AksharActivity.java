//agarg2
//Arpit Garg
//agarg2@andrew.cmu.edu
package edu.cmu.akshar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AksharActivity extends AppCompatActivity {

    AksharActivity me = this;
    Uri searchTerm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * The click listener will need a reference to this object, so that upon successfully extracting text, it
         * can callback to this object with the resulting picture Bitmap.  The "this" of the OnClick will be the OnClickListener, not
         * this AksharActivity.
         */
        final AksharActivity ma = this;

        /*
         * Find the "extract" button and "audio" button, and add a listener to them
         */
        Button audioButton = (Button)findViewById(R.id.audioButton);
        Button extractButton = (Button)findViewById(R.id.extract);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setVisibility(View.VISIBLE);

        int requestcode = 1;
        // https://www.youtube.com/watch?v=go5BdWCKLFk
        // Add a listener to the send button
        audioButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, requestcode);
            }
        });

        extractButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                AksharModel am = new AksharModel();
                am.search(searchTerm, me, ma); // Done asynchronously in another thread.  It calls ip.pictureReady() in this thread when complete.
            }
        });
    }

// adds a select folder request
    public void onActivityResult(int requestcode, int resulCode, Intent data) {
        super.onActivityResult(requestcode, resulCode, data);
        if(requestcode == requestcode && resulCode == Activity.RESULT_OK){
            if(data == null){
                searchTerm = null;
                return;
            }

            Uri uri = data.getData();

            TextView selected = (TextView) findViewById(R.id.selected);
            selected.setText("File selected.");
            selected.setVisibility(View.VISIBLE);

            System.out.println(uri.getPath());

            searchTerm = uri;
        }
    }

    // Set textView as the response
    public void textReady(String response) {
        TextView selected = (TextView) findViewById(R.id.selected);
        if (response != null) {
            // Convert it to JsonObject
            System.out.println(response);
            String body = "{ \"Text\" : " + response + " }";
            System.out.println(body);

            selected.setText(response);
            selected.setVisibility(View.VISIBLE);
            System.out.println(response);
        } else {
            selected.setText("Please select a file.");
            System.out.println("No text");
        }
    }

}