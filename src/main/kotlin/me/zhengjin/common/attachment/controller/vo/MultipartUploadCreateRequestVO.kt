package me.zhengjin.common.attachment.controller.vo

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

class MultipartUploadCreateRequestVO {
    // 文件名称
    @NotEmpty(message = "文件名称不能为空")
    var fileName: String? = null

    // 分片数量
    @NotNull(message = "分片数量不能为空")
    var chunkSize: Int? = null

    // 其他信息
    @NotEmpty(message = "业务模块不能为空")
    var module: String? = null

    @NotEmpty(message = "业务类型不能为空")
    var businessTypeCode: String? = null

    @NotEmpty(message = "业务类型名称不能为空")
    var businessTypeName: String? = null
    var pkId: Long? = null
}
