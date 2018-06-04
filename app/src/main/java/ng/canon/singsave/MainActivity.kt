package ng.canon.singsave

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.sidekick.*
import ng.canon.singsave.Auto.Anchor
import java.util.regex.Pattern
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.net.Uri
import android.support.design.widget.Snackbar
import android.view.View


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbars)
        toolbars.setTitleTextColor(ContextCompat.getColor(applicationContext,android.R.color.white))

        fabs.setOnClickListener {

           watchYoutubeVideo("cCXelCLRl3s")

        }


        paster.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard != null && clipboard.primaryClipDescription.hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            val item = clipboard.primaryClip.getItemAt(0)
            val yourText = item.text.toString()

                if (yourText.contains("https://www.smule.com/")){
                    val tundra = pullLinks(yourText)
                    etSearch.setText(tundra[0])
                }else{

                    val message = getString(R.string.none)
                    snackUp(applicationContext,""+message,header)

                }
          }

        }


        saver.setOnClickListener {

           val name =  etSearch.text.toString()

            if (!name.isEmpty()) {
                scanner(name)
            }else{

                val message = getString(R.string.none)
                snackUp(applicationContext,""+message,header)
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menus, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.getItemId()) {
            R.id.how -> {

                return true
            }
            R.id.download -> {

                val intent = Intent(Intent.ACTION_VIEW)
                intent.type = "video/*, images/*"
                startActivity(intent)

                return true
            }
            R.id.how -> {

                watchYoutubeVideo("cCXelCLRl3s")

                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }

    }


    fun scanner(lorem:String){

        if (lorem.contains("https://www.smule.com/")){
            val ids = pullLinks(lorem)

           val intu = Intent(applicationContext,Anchor::class.java)
            intu.putExtra(Intent.EXTRA_TEXT,ids[0])
            startActivity(intu)

        }else{
            val message = getString(R.string.none)
            snackUp(applicationContext,""+message,header)

        }


    }






    // EXTRACT LINKS FROM STRINGS
    fun pullLinks(text: String): ArrayList<String> {
        val links = ArrayList<String>()
        //String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
        val regex = "\\(?\\b(https?://|www[.]|ftp://)[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]"

        val p = Pattern.compile(regex)
        val m = p.matcher(text)

        while (m.find()) {
            var urlStr = m.group()

            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length - 1)
            }

            links.add(urlStr)
        }

        return links
    }



    //SHOW SNACKBAR
    fun snackUp(context: Context,message:String,view: View)
    {
        val snacks = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snacks.view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
        snacks.show()
    }


    fun watchYoutubeVideo(id: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id))
        val webIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id))
        try {
            startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            startActivity(webIntent)
        }

    }

}
