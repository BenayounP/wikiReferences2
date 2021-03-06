package eu.pbenayoun.basa

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.pbenayoun.repository.referencesrepository.ReferencesSuccessModel
import eu.pbenayoun.basa.databinding.ActivityMainBinding
import eu.pbenayoun.basa.referencerepository.FetchingState
import eu.pbenayoun.basa.referencerepository.ReferencesRepositoryViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var referencesRepositoryViewModel: ReferencesRepositoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        referencesRepositoryViewModel = ViewModelProvider(this).get(ReferencesRepositoryViewModel::class.java)
        setViews()
    }


    private fun setViews() {
        // observers
        referencesRepositoryViewModel.fetchingState.observe(this, { fetchingState ->
            when (fetchingState) {
                is FetchingState.Fetching -> {
                    binding.btnSearch.visibility= View.GONE
                    binding.progressCircular.visibility = View.VISIBLE
                }
                else -> {
                    binding.progressCircular.visibility = View.GONE
                    binding.btnSearch.visibility = View.VISIBLE
                }
            }
            if (fetchingState is FetchingState.Error){
                val snackbarString = getString(R.string.research_error,fetchingState.referencesErrorType.query)
                snackIt(snackbarString)
                referencesRepositoryViewModel.onErrorProcessed()
            }
        })

        referencesRepositoryViewModel.lastReferencesSuccessReferencesModel.observe(this,{ lastSuccessReferencesModel->
            var lastSearchVisibility= when(lastSuccessReferencesModel.references){
                0 -> View.GONE
                else -> View.VISIBLE
            }
            binding.txtLastSearchTitle.visibility=lastSearchVisibility
            binding.txtLastSearchContent.visibility=lastSearchVisibility
            binding.txtLastSearchContent.text=getSearchResultString(lastSuccessReferencesModel)
        })


        // views

        binding.editSearch.doAfterTextChanged {
            referencesRepositoryViewModel.setCurrentQuery(it.toString())
        }

        binding.btnSearch.setOnClickListener { editTextView ->
            // hide Keyboard
            (editTextView.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager).hideSoftInputFromWindow(editTextView.windowToken, 0)
            when {
                referencesRepositoryViewModel.getCurrentQuery().isEmpty() -> snackIt(getString(R.string.empty_query_error))
                else -> referencesRepositoryViewModel.getReferences()
            }
        }
    }

    private fun getSearchResultString(referencesSuccessModel: ReferencesSuccessModel) : String{
        return when(referencesSuccessModel.references)
        {
            //Trick for 0 reference : resources.getQuantityString do not manage 0 quantity properly
            0 -> resources.getString(R.string.last_search_no_result)
            else -> resources.getQuantityString(R.plurals.last_search_result,referencesSuccessModel.references, referencesSuccessModel.query, referencesSuccessModel.references)
        }
    }

    private fun snackIt(snackText: String){
        Snackbar.make(binding.root,snackText, Snackbar.LENGTH_LONG).show()

    }


}