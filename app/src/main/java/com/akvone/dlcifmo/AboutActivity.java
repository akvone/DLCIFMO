package com.akvone.dlcifmo;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initializeTextViews();
        initializeButtons();
    }

    private void initializeTextViews(){
        TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
        versionNumber.setText(Constants.CURRENT_VERSION);

        TextView aboutUniversity = (TextView) findViewById(R.id.aboutUniversity);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/calibribold.ttf");
        aboutUniversity.setTypeface(font);
    }

    private void initializeButtons() {
        ImageView vkButton = (ImageView) findViewById(R.id.vkButton);
        assert vkButton != null;
        vkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.VK_URL));
                startActivity(browserIntent);
            }
        });

        ImageView gmailButton = (ImageView) findViewById(R.id.gmailButton);
        assert gmailButton != null;
        gmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.CONTACT_MAIL});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Отзыв о программе ЦДО ИТМО");
                intent.putExtra(Intent.EXTRA_TEXT, "Я обнаружил в вашей программе следующую проблему... / Я хотел бы поблагодарить вас за...");

                try {
                    startActivity(Intent.createChooser(intent, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView githubButton = (ImageView) findViewById(R.id.githubButton);
        assert githubButton != null;
        githubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GITHUB_URL));
                startActivity(browserIntent);
            }
        });
    }
}