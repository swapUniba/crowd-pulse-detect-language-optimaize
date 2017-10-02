package com.github.frapontillo.pulse.crowd.detectlanguage.optimaize;

import com.github.frapontillo.pulse.crowd.data.entity.Message;
import com.github.frapontillo.pulse.crowd.detectlanguage.ILanguageDetectorOperator;
import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.github.frapontillo.pulse.spi.IPlugin;
import com.github.frapontillo.pulse.spi.VoidConfig;
import rx.Observable;

import java.io.IOException;
import java.util.List;

/**
 * Language detector {@link IPlugin} implementation based on Optimaize.
 * It uses the {@link Message} language to load the appropriate Optimaize file.
 *
 * @author Francesco Pontillo
 */
public class OptimaizeLanguageDetector extends IPlugin<Message, Message, VoidConfig> {
    public final static String PLUGIN_NAME = "optimaize";
    private final LanguageDetector languageDetector;
    private final TextObjectFactory textObjectFactory;

    public OptimaizeLanguageDetector() {
        // load all languages:
        List<LanguageProfile> languageProfiles = null;
        try {
            languageProfiles = new LanguageProfileReader().readAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // build language detector:
        languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles).build();

        // create a text object factory
        textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
    }

    @Override public String getName() {
        return PLUGIN_NAME;
    }

    @Override public VoidConfig getNewParameter() {
        return new VoidConfig();
    }

    @Override public Observable.Operator<Message, Message> getOperator(VoidConfig parameters) {
        return new ILanguageDetectorOperator(this) {
            @Override public String getLanguage(Message message) {
                TextObject textObject = textObjectFactory.forText(message.getText());
                Optional<String> lang = languageDetector.detect(textObject);
                if (lang.isPresent() && !lang.get().equals("und")) {
                    return lang.get();
                }
                return null;
            }
        };
    }
}
