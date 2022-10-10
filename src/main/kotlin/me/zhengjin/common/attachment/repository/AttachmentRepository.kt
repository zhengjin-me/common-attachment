package me.zhengjin.common.attachment.repository

import me.zhengjin.common.attachment.po.Attachment
import me.zhengjin.common.core.repository.BaseRepository

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: AttachmentRepository
 * @description:
 * @date 2019/4/8 16:48
 */
interface AttachmentRepository : BaseRepository<Attachment, Long> {

    fun findByIdAndDeleteFalse(id: Long): Attachment?

    fun findAllByIdInAndDeleteFalse(ids: List<Long>): List<Attachment>

    fun findAllByPkIdAndModuleAndDeleteFalse(pkId: Long, module: String): List<Attachment>

    fun findAllByPkIdAndModuleAndBusinessTypeCodeAndDeleteFalse(pkId: Long, module: String, businessTypeCode: String): List<Attachment>
}
