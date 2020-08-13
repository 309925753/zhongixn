package com.vkzwbim.chat.bean;

import java.util.List;

public class AdvEntivity {


    /**
     * currentTime : 1589540075399
     * data : [{"createTime":"2020-05-15 18:31:09","id":"553de83d863d4cdfaa2a0d9dae347103","img":"http://file.jiujiuim.com/u/1000/1000/202005/o/2e56085ebadc4a82ba62d52ea2abaf84.jpg","isShow":"0","title":"上海行杨商贸有限公司","url":"http://www.taobao.com"}]
     * resultCode : 1
     */

    private long currentTime;
    private int resultCode;
    private List<DataBean> data;

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * createTime : 2020-05-15 18:31:09
         * id : 553de83d863d4cdfaa2a0d9dae347103
         * img : http://file.jiujiuim.com/u/1000/1000/202005/o/2e56085ebadc4a82ba62d52ea2abaf84.jpg
         * isShow : 0
         * title : 上海行杨商贸有限公司
         * url : http://www.taobao.com
         */

        private String createTime;
        private String id;
        private String img;
        private String isShow;
        private String title;
        private String url;

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getIsShow() {
            return isShow;
        }

        public void setIsShow(String isShow) {
            this.isShow = isShow;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
