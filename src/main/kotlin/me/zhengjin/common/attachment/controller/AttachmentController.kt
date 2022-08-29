package me.zhengjin.common.attachment.controller

import me.zhengjin.common.attachment.adapter.AttachmentStorage
import me.zhengjin.common.attachment.controller.vo.AttachmentVO
import me.zhengjin.common.attachment.controller.vo.MergeDownloadVO
import me.zhengjin.common.attachment.po.AttachmentModelHelper
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
        @RequestParam(value = "pkId", required = false) pkId: String?
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
     * 文件列表
     *
     * @param module
     * @param pkId
     * @return
     */
    @GetMapping("/list")
    fun fileList(
        @RequestParam("module") module: String,
        @RequestParam("pkId") pkId: String,
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
    fun deleteBatch(@RequestBody ids: List<String>): HttpResult<String> {
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
    fun view(response: HttpServletResponse, @PathVariable("id") id: String) {
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
    fun download(response: HttpServletResponse, @PathVariable("id") id: String) {
        attachmentStorage.download(response, id, true)
    }

    /**
     * 文件下载合并为zip
     *
     * @param response
     * @throws IOException
     */
    @PostMapping("/download/zip")
    fun mergeDownload(@RequestBody ids: List<String>, response: HttpServletResponse): HttpResult<MergeDownloadVO> {
        return HttpResult.ok(attachmentStorage.mergeDownload(ids, response))
    }

    /**
     * 文件外链
     * @param id
     */
    @GetMapping("/share/{id}")
    fun share(
        @PathVariable("id") id: String
    ): HttpResult<String> {
        return HttpResult.ok(body = attachmentStorage.share(id))
    }
}
