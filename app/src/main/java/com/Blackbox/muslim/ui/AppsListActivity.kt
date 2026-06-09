package com.Blackbox.muslim.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Blackbox.muslim.R
import com.Blackbox.muslim.data.AppPreferences

class AppsListActivity : AppCompatActivity() {

    private lateinit var preferences: AppPreferences
    private lateinit var appsProvider: InstalledAppsProvider
    private var blockedApps = mutableSetOf<String>()
    private var unblockedApps = mutableSetOf<String>()
    private var mode = "blocked"

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSave: Button
    private lateinit var switchAll: Switch
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var llContent: LinearLayout
    private var adapter: AppsAdapter? = null
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        preferences = AppPreferences(this)
        setTheme(AppPreferences.getThemeResourceId(preferences.getTheme()))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps_list)
        appsProvider = InstalledAppsProvider(this)

        mode = intent.getStringExtra("mode") ?: "blocked"
        blockedApps = preferences.getBlockedApps().toMutableSet()
        unblockedApps = preferences.getUnblockedApps().toMutableSet()

        tvTitle = findViewById(R.id.tvAppsTitle)
        tvSubtitle = findViewById(R.id.tvAppsSubtitle)
        recyclerView = findViewById(R.id.rvApps)
        btnSave = findViewById(R.id.btnSaveApps)
        switchAll = findViewById(R.id.switchAllApps)
        progressBar = findViewById(R.id.progressLoading)
        llContent = findViewById(R.id.llContent)

        if (isFirstLoad) {
            tvTitle.visibility = View.VISIBLE
            tvSubtitle.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            btnSave.visibility = View.GONE
            switchAll.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            showSkeletonLoading()
            isFirstLoad = false
        }

        appsProvider.getInstalledApps { apps ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE
                switchAll.visibility = View.VISIBLE
                setupUI(apps)
            }
        }
    }

    private fun showSkeletonLoading() {
        val skeletonList = mutableListOf<Any>()
        repeat(10) { skeletonList.add(Any()) }

  recyclerView.layoutManager = LinearLayoutManager(this)
  recyclerView.adapter = SkeletonAdapter()
}

private class SkeletonAdapter : RecyclerView.Adapter<SkeletonAdapter.SkeletonVH>() {
  inner class SkeletonVH(view: View) : RecyclerView.ViewHolder(view)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkeletonVH {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_skeleton, parent, false)
      return SkeletonVH(view)
  }

  override fun onBindViewHolder(holder: SkeletonVH, position: Int) {}

  override fun getItemCount() = 10
}

    private fun setupUI(apps: List<InstalledApp>) {
        if (mode == "blocked") {
            tvTitle.text = "تطبيقات محظورة أثناء الصلاة"
            tvSubtitle.text = "اختر التطبيقات التي تريد حظرها"
        } else {
            tvTitle.text = "تطبيقات مسموح بها أثناء الصلاة"
            tvSubtitle.text = "اختر التطبيقات التي لا يتم حظرها"
        }

        recyclerView = findViewById(R.id.rvApps)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AppsAdapter(
            apps,
            if (mode == "blocked") blockedApps else unblockedApps
        )
        recyclerView.adapter = adapter

        switchAll.setOnCheckedChangeListener { _, isChecked ->
            val allPackages = apps.map { it.packageName }.toSet()
            if (isChecked) {
                if (mode == "blocked") {
                    blockedApps.addAll(allPackages)
                } else {
                    unblockedApps.addAll(allPackages)
                }
            } else {
                if (mode == "blocked") {
                    blockedApps.clear()
                } else {
                    unblockedApps.clear()
                }
            }
            adapter?.notifyDataSetChanged()
        }

        btnSave.setOnClickListener {
            if (mode == "blocked") {
                preferences.setBlockedApps(blockedApps)
            } else {
                preferences.setUnblockedApps(unblockedApps)
            }
            Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    inner class AppsAdapter(
        private val apps: List<InstalledApp>,
        private val selectedApps: MutableSet<String>
    ) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

        inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.ivAppIcon)
            val name: TextView = view.findViewById(R.id.tvAppName)
            val packageName: TextView = view.findViewById(R.id.tvAppPackage)
            val checkBox: CheckBox = view.findViewById(R.id.cbAppSelected)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            val app = apps[position]
            holder.name.text = app.name
            holder.packageName.text = app.packageName
            holder.icon.setImageDrawable(app.icon)
            holder.checkBox.isChecked = selectedApps.contains(app.packageName)

            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedApps.add(app.packageName)
                } else {
                    selectedApps.remove(app.packageName)
                }
            }

            holder.itemView.setOnClickListener {
                holder.checkBox.isChecked = !holder.checkBox.isChecked
            }
        }

        override fun getItemCount() = apps.size
    }
}
