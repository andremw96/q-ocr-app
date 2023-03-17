package com.example.qocrapp

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.qocrapp.api.ChatGptService
import com.example.qocrapp.api.CompletionRequest
import com.example.qocrapp.databinding.ResultFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ResultFragment(
    private val extractedText: String,
    private val freeText: String? = null,
    private val actionGenerate: ActionGenerate,
) : DialogFragment() {

    private var _binding: ResultFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var chatGptService: ChatGptService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ResultFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textView.movementMethod = ScrollingMovementMethod()

        binding.progressCircular.visibility = View.VISIBLE
        isCancelable = false
        GlobalScope.launch(Dispatchers.IO) {
            val prompt = if (actionGenerate == ActionGenerate.TRANSLATE) {
                "${context?.getString(actionGenerate.stringResource)} Translate into $freeText: $extractedText"
            } else if (actionGenerate == ActionGenerate.FREE_TEXT && freeText != null) {
                "$freeText: $extractedText"
            } else {
                "${context?.getString(actionGenerate.stringResource)}: $extractedText"
            }

            val result = chatGptService.completions(
                CompletionRequest(
                    prompt = prompt.replace("\n", " ")
                )
            )
            GlobalScope.launch(Dispatchers.Main) {
                isCancelable = true
                when (result.code()) {
                    200 -> {
                        binding.textView.text = "${result.body()!!.choices[0].text}"
                        binding.progressCircular.visibility = View.GONE
                    }
                    else -> {
                        Log.e("ResultFragment", "Error Summarizing ${result.message()}")
                        binding.progressCircular.visibility = View.GONE
                    }
                }
            }
        }
    }
}
