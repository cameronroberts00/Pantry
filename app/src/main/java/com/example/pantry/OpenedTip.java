package com.example.pantry;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.Objects;


public class OpenedTip extends Fragment {
View view;
TextView nameText;
TextView bodyText;
ImageView imageView;
Button back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_opened_tip, container, false);
        nameText=view.findViewById(R.id.name_text);
        bodyText=view.findViewById(R.id.body_text);
        imageView=view.findViewById(R.id.image_view);
        back=view.findViewById(R.id.back);

        back.setOnClickListener(listener);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String defaultValue="";
            String name = bundle.getString("name", defaultValue);
            String body = bundle.getString("body", defaultValue);
            String image = bundle.getString("image",defaultValue);

            nameText.setText(name);
            bodyText.setText(body);
            Glide.with(getContext()).load(image).into(imageView);
            Log.d("TAG", "Attributes loaded are"+name+body+image);
        }

        //Glide.with(getActivity()).load("https://upload.wikimedia.org/wikipedia/commons/a/a4/Anatomy_of_a_Sunset-2.jpg").into(imageView);



        return view;
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.back:
                    Home home = new Home();
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame,home).addToBackStack(null).commit();
                    break;

            }
        }
    };
}