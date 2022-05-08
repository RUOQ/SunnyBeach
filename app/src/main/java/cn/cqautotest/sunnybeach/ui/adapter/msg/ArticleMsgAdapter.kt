package cn.cqautotest.sunnybeach.ui.adapter.msg

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cn.cqautotest.sunnybeach.databinding.ArticleMsgListItemBinding
import cn.cqautotest.sunnybeach.ktx.setFixOnClickListener
import cn.cqautotest.sunnybeach.model.msg.ArticleMsg
import cn.cqautotest.sunnybeach.ui.adapter.AdapterDelegate
import com.blankj.utilcode.util.TimeUtils

/**
 * author : A Lonely Cat
 * github : https://github.com/anjiemo/SunnyBeach
 * time   : 2021/10/25
 * desc   : 文章评论列表消息适配器
 */
class ArticleMsgAdapter(private val adapterDelegate: AdapterDelegate) :
    PagingDataAdapter<ArticleMsg.Content, ArticleMsgAdapter.ArticleMsgViewHolder>(
        ArticleMsgDiffCallback()
    ) {

    class ArticleMsgDiffCallback : DiffUtil.ItemCallback<ArticleMsg.Content>() {
        override fun areItemsTheSame(
            oldItem: ArticleMsg.Content,
            newItem: ArticleMsg.Content
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ArticleMsg.Content,
            newItem: ArticleMsg.Content
        ): Boolean {
            return oldItem == newItem
        }
    }

    inner class ArticleMsgViewHolder(val binding: ArticleMsgListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onViewAttachedToWindow(holder: ArticleMsgViewHolder) {
        super.onViewAttachedToWindow(holder)
        adapterDelegate.onViewAttachedToWindow(holder)
    }

    override fun onBindViewHolder(holder: ArticleMsgViewHolder, position: Int) {
        val itemView = holder.itemView
        val binding = holder.binding
        val ivAvatar = binding.ivAvatar
        val cbNickName = binding.cbNickName
        val tvDesc = binding.tvDesc
        val tvReplyMsg = binding.tvReplyMsg
        val tvChildReplyMsg = binding.tvChildReplyMsg
        val item = getItem(position) ?: return
        itemView.setFixOnClickListener {
            adapterDelegate.onItemClick(it, position)
        }
        ivAvatar.loadAvatar(false, item.avatar)
        cbNickName.text = item.nickname
        val sdf = TimeUtils.getSafeDateFormat("yyyy-MM-dd HH:mm")
        tvDesc.text = TimeUtils.getFriendlyTimeSpanByNow(item.createTime, sdf)
        tvReplyMsg.text = HtmlCompat.fromHtml(item.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
        tvChildReplyMsg.text = item.title
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleMsgViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ArticleMsgListItemBinding.inflate(inflater, parent, false)
        return ArticleMsgViewHolder(binding)
    }
}