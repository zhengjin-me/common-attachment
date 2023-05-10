package me.zhengjin.common.attachment.controller.vo

import java.util.LinkedList

class MultipartUploadCreateResponseVO {
    // 分片ID
    var uploadId: String? = null
    // 分片数量
    var chunks: MutableList<UploadPartItem> = LinkedList()

    class UploadPartItem {
        var partNo: Int? = null
        var uploadUrl: String? = null
    }
}
