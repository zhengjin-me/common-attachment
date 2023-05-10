package me.zhengjin.common.attachment.controller.vo

import me.zhengjin.common.attachment.po.Attachment
import me.zhengjin.common.core.entity.IdEntity
import me.zhengjin.common.utils.BeanFieldCopyUtils

class AttachmentVO : IdEntity() {

    /**
     * 所属业务单元
     */
    var module: String? = null

    /**
     * 业务类型
     */
    var businessTypeCode: String? = null

    var businessTypeName: String? = null

    /**
     * 原始文件名
     */
    var fileOriginName: String? = null

    /**
     * 资源服务器文件相对路径
     */
    var filePath: String? = null

    /**
     * 文件类型 mimeType
     */
    var fileType: String? = null

    /**
     * 文件大小 单位 字节
     */
    var fileSize: Long? = null

    /**
     * 是否只读
     * 只读文件不可下载 仅可通过图片在线预览
     */
    var readOnly: Boolean = false

    /**
     * 下载地址
     * 根据具体存储实现视情况返回, 例如本地存储则为空
     */
    var url: String? = null

    companion object {
        fun transform(attachment: Attachment): AttachmentVO {
            val vo = AttachmentVO()
            BeanFieldCopyUtils.copyProperties(
                attachment,
                vo,
                "id", "module", "businessTypeCode", "businessTypeName",
                "fileOriginName", "filePath", "fileType", "fileSize", "readOnly",
            )
            return vo
        }
    }
}
