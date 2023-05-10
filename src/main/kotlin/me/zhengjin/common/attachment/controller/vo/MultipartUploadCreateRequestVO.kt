package me.zhengjin.common.attachment.controller.vo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MultipartUploadCreateRequestVO {
    // 文件名称
    var fileName: String? = null
    // 分片数量
    var chunkSize: Int? = null
}
