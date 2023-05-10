package me.zhengjin.common.attachment.controller.vo

class CompleteMultipartUploadRequestVO {
    // 文件名称
    var fileName: String? = null
    // 分片ID
    var uploadId: String? = null
    // 分片数量
    var chunkSize: Int? = null
}
