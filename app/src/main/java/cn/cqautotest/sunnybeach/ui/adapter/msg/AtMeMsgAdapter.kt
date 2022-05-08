package cn.cqautotest.sunnybeach.ui.adapter.msg

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cn.cqautotest.sunnybeach.databinding.AtMeMsgListItemBinding
import cn.cqautotest.sunnybeach.ktx.setFixOnClickListener
import cn.cqautotest.sunnybeach.model.msg.AtMeMsg
import cn.cqautotest.sunnybeach.ui.adapter.AdapterDelegate
import com.blankj.utilcode.util.TimeUtils

/**
 * author : A Lonely Cat
 * github : https://github.com/anjiemo/SunnyBeach
 * time   : 2021/10/25
 * desc   : @我 列表消息适配器
 */
class AtMeMsgAdapter(private val adapterDelegate: AdapterDelegate) :
    PagingDataAdapter<AtMeMsg.Content, AtMeMsgAdapter.AtMeMsgViewHolder>(AtMeMsgDiffCallback()) {

    class AtMeMsgDiffCallback : DiffUtil.ItemCallback<AtMeMsg.Content>() {
        override fun areItemsTheSame(
            oldItem: AtMeMsg.Content,
            newItem: AtMeMsg.Content
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: AtMeMsg.Content,
            newItem: AtMeMsg.Content
        ): Boolean {
            return oldItem == newItem
        }
    }

    inner class AtMeMsgViewHolder(val binding: AtMeMsgListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onViewAttachedToWindow(holder: AtMeMsgViewHolder) {
        super.onViewAttachedToWindow(holder)
        adapterDelegate.onViewAttachedToWindow(holder)
    }

    override fun onBindViewHolder(holder: AtMeMsgViewHolder, position: Int) {
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
        tvDesc.text = TimeUtils.getFriendlyTimeSpanByNow(item.publishTime, sdf)
        tvReplyMsg.height = 0
        tvChildReplyMsg.text = HtmlCompat.fromHtml(item.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AtMeMsgViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AtMeMsgListItemBinding.inflate(inflater, parent, false)
        return AtMeMsgViewHolder(binding)
    }
}