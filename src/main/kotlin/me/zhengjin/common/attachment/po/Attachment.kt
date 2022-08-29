package me.zhengjin.common.attachment.po

import com.fasterxml.jackson.annotation.JsonIgnore
import me.zhengjin.common.core.entity.BaseEntity
import me.zhengjin.common.core.exception.ServiceException
import me.zhengjin.common.core.jpa.comment.annotation.JpaComment
import java.util.concurrent.ConcurrentHashMap
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * @version V1.0
 * @Title: Attachment
 * @Package
 * @Description: 公共附件
 * @Author fangzhengjin
 * @Date 2018-3-29 16:49
 */
@Entity
@Table(name = "pub_attachment")
@JpaComment("公共附件")
class Attachment : BaseEntity() {

    /**
     * 业务主键
     */
    @JpaComment("业务主键")
    @JsonIgnore
    @Column
    var pkId: String? = null

    /**
     * 所属业务单元
     */
    @JpaComment("所属业务单元")
    @JsonIgnore
    @Column
    var module: String? = null

    /**
     * 业务类型
     */
    @JpaComment("业务类型代码")
    @Column
    var businessTypeCode: String? = null

    @JpaComment("业务类型名称")
    @Column
    var businessTypeName: String? = null

    /**
     * 原始文件名
     */
    @JpaComment("原始文件名")
    @Column
    var fileOriginName: String? = null

    /**
     * 资源服务器文件相对路径
     */
    @JpaComment("资源服务器文件相对路径")
    @JsonIgnore
    @Column
    var filePath: String? = null

    /**
     * 文件类型 mimeType
     */
    @JpaComment("文件类型 mimeType")
    @Column
    var fileType: String? = null

    /**
     * 文件大小 单位 字节
     */
    @JpaComment("文件大小 单位 字节")
    @Column
    var fileSize: String? = null

    /**
     * 是否只读
     * 只读文件不可下载 仅可通过图片在线预览
     */
    @JpaComment("是否只读")
    @JsonIgnore
    @Column
    var readOnly: Boolean = false
}

/**
 * 这里是附件所属的模块信息
 * 不是附件类型
 */
data class AttachmentModel(
    var modelCode: String,
    var modelName: String,
    var businessType: MutableMap<String, String>? = null,
)

object AttachmentModelHelper {
    private val models: MutableMap<String, AttachmentModel> = ConcurrentHashMap()

    init {
        // 其他公用附件 无pkId
        // 没有注册businessType, 所以不会进行校验
        registerModel(
            "OTHER",
            "其他业务"
        )
    }

    /**
     * 注册模块与业务类型
     */
    fun registerModel(
        modelCode: String,
        modelName: String,
        vararg businessType: Pair<String, String>,
    ) {
        registerModel(
            AttachmentModel(
                modelCode,
                modelName,
                businessType.toMap().toMutableMap()
            )
        )
    }

    /**
     * 注册模块与业务类型
     */
    fun registerModel(attachmentModel: AttachmentModel) {
        val modelCode = attachmentModel.modelCode.uppercase()
        if (!models.containsKey(modelCode)) {
            models[modelCode] = attachmentModel
        } else {
            if (!attachmentModel.businessType.isNullOrEmpty()) {
                val model = models[modelCode]!!
                if (model.businessType.isNullOrEmpty()) {
                    model.businessType = attachmentModel.businessType!!.map {
                        it.key to it.value.uppercase()
                    }.toMap().toMutableMap()
                } else {
                    model.businessType!!.putAll(
                        attachmentModel.businessType!!.map {
                            it.key to it.value.uppercase()
                        }.toMap()
                    )
                }
            }
        }
    }

    /**
     * @param modelCode 模块代码
     */
    fun checkRegister(modelCode: String, businessType: String? = null) {
        val attachmentModel = models[modelCode.uppercase()]
        ServiceException.requireNotNull(attachmentModel) {
            "model [$modelCode] can not be found"
        }
        // 注册了businessType并且提供了需要校验的businessType才会进行校验
        if (!businessType.isNullOrBlank() && !attachmentModel!!.businessType.isNullOrEmpty())
            ServiceException.requireTrue(attachmentModel.businessType!!.containsKey(businessType)) {
                "businessType [$businessType] can not be found"
            }
    }
}
