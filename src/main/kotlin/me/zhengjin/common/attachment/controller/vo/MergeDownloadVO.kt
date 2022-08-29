package me.zhengjin.common.attachment.controller.vo

class MergeDownloadVO(
    /**
     * 文件名称
     */
    var filename: String? = null,
    /**
     * 文件内容 流=>base64
     */
    var content: String? = null
)
