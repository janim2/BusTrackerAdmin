package com.safet.admin.bustrackeradmin.Adapters;

import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

public class image_slider_adapter extends SliderAdapter {
    String image_one, image_two, image_three;

    public image_slider_adapter(String imageone, String imagetwo, String imagethree){
        this.image_one = imageone;
        this.image_two = imagetwo;
        this.image_three = imagethree;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public void onBindImageSlide(int position, ImageSlideViewHolder viewHolder) {
        switch (position) {
            case 0:
//                viewHolder.bindImageSlide("https://www.cdnis.edu.hk/sites/default/files/styles/1200x795/public/CDNIS%20solar%20panel_1.jpg?itok=qCVODOAq");
                viewHolder.bindImageSlide(image_one);
                break;
            case 1:
//                viewHolder.bindImageSlide("https://studysive.com/wp-content/uploads/2019/08/IMG_6482av-cropped.jpg");
                viewHolder.bindImageSlide(image_two);
                break;
            case 2:
//                viewHolder.bindImageSlide("https://www.al.com/resizer/ceGxncEJ-FXZLbQo_kWb9aCOUKM=/1280x0/smart/advancelocal-adapter-image-uploads.s3.amazonaws.com/image.al.com/home/bama-media/width2048/img/spotnews/photo/gardendale-high-school-d22ee872679c54d7.jpg");
                viewHolder.bindImageSlide(image_three);
                break;
        }
    }
}
