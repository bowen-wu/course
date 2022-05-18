package com.personal.course.entity.VO;

import com.personal.course.entity.DO.Video;
import com.personal.course.entity.base.VideoBase;

public class VideoVO extends VideoBase {
    public VideoVO() {
    }

    public VideoVO(Video videoInDb) {
        super(videoInDb.getId(), videoInDb.getCreatedOn(), videoInDb.getUpdatedOn(), videoInDb.getStatus(), videoInDb.getName(), videoInDb.getDescription());
    }
}
