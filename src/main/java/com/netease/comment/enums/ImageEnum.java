package com.netease.comment.enums;

public enum ImageEnum {
    TIFF("image/tiff", ".tif"),
    FAX("image/fax", ".fax"),
    GIF("image/gif", ".gif"),
    XICON("image/x-icon", ".ico"),
    JPG("image/jpeg", ".jpg"),
    NET("image/pnetvue", ".net"),
    PNG("image/png", ".png"),
    RP("image/vnd.rn-realpix", ".rp"),
    WBMP("image/vnd.wap.wbmp", ".wbmp");

    private String extendName;

    private String contentName;

    ImageEnum(String contentName, String extendName) {
        this.contentName = contentName;
        this.extendName = extendName;
    }

    public static String getExtendName(String name) {
        for (ImageEnum image : ImageEnum.values()) {
            if (image.contentName.equalsIgnoreCase(name)) {
                return image.extendName;
            }
        }
        return JPG.extendName;
    }

}

