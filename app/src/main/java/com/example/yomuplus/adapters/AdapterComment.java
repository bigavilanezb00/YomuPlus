package com.example.yomuplus.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yomuplus.MyApplication;
import com.example.yomuplus.R;
import com.example.yomuplus.databinding.RowCommentBinding;
import com.example.yomuplus.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.HolderComment>{

    private Context context;
    //array list para los comentarios
    private ArrayList<ModelComment> commentArrayList;

    private FirebaseAuth firebaseAuth;

    private RowCommentBinding binding;

    // constructor
    public AdapterComment(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {
        // Obtener datos
        ModelComment modelComment = commentArrayList.get(position);
        String id = modelComment.getId();
        String bookId = modelComment.getBookId();
        String comment = modelComment.getComment();
        String uid = modelComment.getUid();
        String timestamp = modelComment.getTimestamp();

        // formato de la fecha de myapplication
        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

        // colocar datos
        holder.dateTv.setText(date);
        holder.commentTv.setText(comment);

        loadUserDetails(modelComment, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() != null && uid.equals(firebaseAuth.getUid())) {
                    deleteComment(modelComment, holder);
                }
            }
        });
    }

    private void deleteComment(ModelComment modelComment, HolderComment holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminar comentario")
                .setMessage("¿Estás seguro de que quieres eliminar este comentrio?")
                .setPositiveButton("ELIMINAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Libros");
                        ref.child(modelComment.getBookId())
                                .child("Comments")
                                .child(modelComment.getId())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Eliminando", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Fallo al eliminar el comentario debido a "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void loadUserDetails(ModelComment modelComment, HolderComment holder) {
        String uid = modelComment.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        holder.nameTv.setText(name);
                        try {
                            Glide.with(context)
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_persona_gray)
                                    .into(holder.profileIv);

                        } catch (Exception e) {
                            holder.profileIv.setImageResource(R.drawable.ic_persona_gray);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return commentArrayList.size(); // devuelve los comentarios
    }

    // view holder para el row_comment
    class HolderComment extends RecyclerView.ViewHolder {

        ShapeableImageView profileIv;
        TextView nameTv, dateTv, commentTv;

        public HolderComment(@NonNull View itemView) {
            super(itemView);

            profileIv = binding.profileIv;
            nameTv = binding.nameTv;
            dateTv = binding.dateTv;
            commentTv = binding.commentTv;
        }
    }
}
