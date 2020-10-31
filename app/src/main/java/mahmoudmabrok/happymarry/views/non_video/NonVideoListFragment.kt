package mahmoudmabrok.happymarry.views.non_video

import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_articles_list.*
import kotlinx.android.synthetic.main.fragment_articles_list.rvItems
import kotlinx.android.synthetic.main.fragment_articles_list.swip
import kotlinx.android.synthetic.main.fragment_movies_list.*
import mahmoudmabrok.happymarry.R
import mahmoudmabrok.happymarry.base.BaseFragment
import mahmoudmabrok.happymarry.dataLayer.AppRepo
import mahmoudmabrok.happymarry.dataLayer.models.NonVideo
import mahmoudmabrok.happymarry.dataLayer.models.NonVideosResponse
import mahmoudmabrok.happymarry.util.Logger
import mahmoudmabrok.happymarry.viewholders.NonVideoVH
import mahmoudmabrok.happymarry.viewmodels.VideoListViewmodel
import mahmoudmabrok.happymarry.views.nonVideoDetails.NonVideoDetailFragment
import me.ibrahimyilmaz.kiel.adapterOf

class NonVideoListFragment : BaseFragment(R.layout.fragment_articles_list) {

    private val model by activityViewModels<VideoListViewmodel>()
    private val adapter = adapterOf<NonVideo> {
        register(
            layoutResource = R.layout.item_non_video,
            viewHolder = ::NonVideoVH,
            onViewHolderCreated = { vh ->
                vh.itemView.setOnClickListener {
                    model.nonVideoItem = vh.data
                    show(NonVideoDetailFragment())
                }
            },
            onBindViewHolder = { vh: NonVideoVH, pos: Int, p: NonVideo ->
                vh.bind(p)
            }
        )
    }

    private val repo by lazy { AppRepo() }

    override fun initViews() {
        rvItems?.adapter = adapter
    }

    override fun loadData() {
        super.loadData()
        spLoading.visibility = View.VISIBLE
        repo.loadNonVideos()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Logger.log("data ${it.size}")
                handleData(it)
                spLoading.visibility = View.GONE
                swip.isRefreshing = false
            }, {
                Logger.log("Error ${it.message}")
                handleError(it.message)
            })
            .also {
                bag.add(it)
            }
    }

    private fun handleError(message: String?) {
        Logger.log("Error $message")
        spVideos.visibility = View.GONE
        if (message?.contains("resolve") == true) {
            Toast.makeText(requireContext(), "تاكد من اتصالك بالانترنت", Toast.LENGTH_SHORT).show()
        } else
            Toast.makeText(requireContext(), "حدث خطأ", Toast.LENGTH_SHORT).show()
        swip.isRefreshing = false
    }

    private fun handleData(it: NonVideosResponse?) {
        it?.let {
            adapter.submitList(it)
        }
    }
}