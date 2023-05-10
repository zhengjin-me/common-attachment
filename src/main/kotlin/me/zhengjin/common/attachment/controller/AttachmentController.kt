package me.zhengjin.common.attachment.controller

import me.zhengjin.common.attachment.adapter.AttachmentStorage
import me.zhengjin.common.attachment.controller.vo.AttachmentVO
import me.zhengjin.common.attachment.controller.vo.CompleteMultipartUploadRequestVO
import me.zhengjin.common.attachment.controller.vo.MergeDownloadVO
import me.zhengjin.common.attachment.controller.vo.MultipartUploadCreateRequestVO
import me.zhengjin.common.attachment.controller.vo.MultipartUploadCreateResponseVO
import me.zhengjin.common.attachment.po.AttachmentModelHelper
import me.zhengjin.common.core.encryptor.annotation.IdDecrypt
import me.zhengjin.common.core.entity.HttpResult
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

/**
 * @version V1.0
 * title: 通用文件上传下载处理
 * package
 * description:
 * @author fangzhengjin
 * cate 2018-7-26 16:20
 */
@RestController
@RequestMapping("/file", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
class AttachmentController(
    private val attachmentStorage: AttachmentStorage
) {

    /**
     * 文件上传
     *
     * @param file
     * @param module
     * @param businessTypeCode
     * @param businessTypeName
     * @param pkId
     */
    @PostMapping("/upload")
    @Throws(IOException::class)
    fun fileUpload(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("module") module: String,
        @RequestParam("businessTypeCode") businessTypeCode: String,
        @RequestParam("businessTypeName") businessTypeName: String,
        @IdDecrypt @RequestParam(value = "pkId", required = false) pkId: Long?
    ): HttpResult<AttachmentVO> {
        AttachmentModelHelper.checkRegister(module, businessTypeCode)
        return HttpResult.ok(
            attachmentStorage.saveFiles(
                file,
                module,
                businessTypeCode,
                businessTypeName,
                pkId
            )
        )
    }

    /**
     * 创建分片上传
     */
    @PostMapping("/file/multipart/create")
    fun createMultipartUpload(@RequestBody @Valid vo: MultipartUploadCreateRequestVO): HttpResult<MultipartUploadCreateResponseVO> {
        return HttpResult.ok(attachmentStorage.createMultipartUpload(vo))
    }

    /**
     * 合并分片数据
     */
    @PostMapping("/file/multipart/complete")
    fun completeMultipartUpload(@RequestBody @Valid vo: CompleteMultipartUploadRequestVO): HttpResult<AttachmentVO> {
        return HttpResult.ok(attachmentStorage.completeMultipartUpload(vo))
    }

    /**
     * 文件列表
     *
     * @param module
     * @param pkId
     * @return
     */
    @GetMapping("/list")
    fun fileList(
        @RequestParam("module") module: String,
        @IdDecrypt @RequestParam("pkId") pkId: Long,
        @RequestParam("readOnly", required = false) readOnly: Boolean?,
        @RequestParam("businessTypeCode", required = false) businessTypeCode: List<String>?,
    ): HttpResult<List<AttachmentVO>> {
        AttachmentModelHelper.checkRegister(module)
        return HttpResult.ok(
            attachmentStorage.list(
                module,
                pkId,
                readOnly ?: false,
                *(businessTypeCode ?: listOf()).toTypedArray()
            )
        )
    }

    /**
     * 批量删除
     */
    @PostMapping("/delete/batch")
    fun deleteBatch(@IdDecrypt @RequestBody ids: List<Long>): HttpResult<String> {
        attachmentStorage.deleteBatch(ids)
        return HttpResult.ok()
    }

    /**
     * 文件预览
     *
     * @param response
     * @param id
     * @throws IOException
     */
    @GetMapping("/view/{id}")
    @Throws(IOException::class)
    fun view(response: HttpServletResponse, @IdDecrypt @PathVariable("id") id: Long) {
        attachmentStorage.download(response, id, false)
    }

    /**
     * 文件下载
     *
     * @param response
     * @param id
     * @throws IOException
     */
    @GetMapping("/download/{id}")
    @Throws(IOException::class)
    fun download(response: HttpServletResponse, @IdDecrypt @PathVariable("id") id: Long) {
        attachmentStorage.download(response, id, true)
    }

    /**
     * 文件下载合并为zip
     *
     * @param response
     * @throws IOException
     */
    @PostMapping("/download/zip")
    fun mergeDownload(
        @IdDecrypt @RequestBody ids: List<Long>,
        response: HttpServletResponse
    ): HttpResult<MergeDownloadVO> {
        return HttpResult.ok(attachmentStorage.mergeDownload(ids, response))
    }

    /**
     * 文件外链
     * @param id
     */
    @GetMapping("/share/{id}")
    fun share(@IdDecrypt @PathVariable("id") id: Long): HttpResult<String> {
        return HttpResult.ok(body = attachmentStorage.share(id))
    }
}
