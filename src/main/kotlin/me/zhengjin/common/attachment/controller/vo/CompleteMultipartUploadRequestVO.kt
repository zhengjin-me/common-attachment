package me.zhengjin.common.attachment.controller.vo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class CompleteMultipartUploadRequestVO {
    // 文件名称
    var fileName: String? = null
    // 分片ID
    var uploadId: String? = null
    // 分片数量
    var chunkSize: Int? = null
    // 只读!!!文件存储地址
    val filePath: String
        get() = String.format(
            "%s/%s",
            DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.CHINA).format(LocalDateTime.now()),
            fileName
        )
}
