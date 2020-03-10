package com.example.planeacionapp.ui.send;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.planeacionapp.EstadoActivity;
import com.example.planeacionapp.MainActivity;
import com.example.planeacionapp.R;

public class SendFragment extends Fragment {

    private SendViewModel sendViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        sendViewModel =
                ViewModelProviders.of(this).get(SendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_send, container, false);
        final TextView textView = root.findViewById(R.id.text_send);
        sendViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                ((MainActivity) getActivity()).getmCallout().dismiss();

                EstadoActivity estadoActivity = new EstadoActivity();
                estadoActivity.show(((MainActivity) getActivity()).getSupportFragmentManager(), EstadoActivity.TAG);

                //textView.setText(s);
            }
        });
        return root;
    }
}