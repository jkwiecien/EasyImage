package pl.aprilapps.easypicker.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import pl.aprilapps.easypicker.MediaFile;


/**
 * Created by Jacek Kwiecień on 08.11.2016.
 */

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private Context context;
    private List<MediaFile> imagesFiles;

    public ImagesAdapter(Context context, List<MediaFile> imagesFiles) {
        this.context = context;
        this.imagesFiles = imagesFiles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ViewHolder(inflater.inflate(R.layout.view_image, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Picasso.get()
                .load(imagesFiles.get(position).getFile())
                .fit()
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imagesFiles.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }


    }
}
