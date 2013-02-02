package ecologylab.bigsemantics.service.crawler;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import ecologylab.bigsemantics.generated.library.acm.AcmPortal;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.serialization.SIMPLTranslationException;

public class TestScholarlyArticleCrawler extends Assert
{

  @Test
  public void TestGetScholarlyArticleFromService() throws SIMPLTranslationException, IOException
  {
    ScholarlyArticleCrawler sae = new ScholarlyArticleCrawler(null);
    Document doc = sae.getScholarlyArticleFromService("http://dl.acm.org/citation.cfm?id=1242672");
    assertNotNull(doc);
    assertNotNull(doc.getMetaMetadata());
    assertEquals("acm_portal", doc.getMetaMetadata().getName());
    assertTrue(doc instanceof AcmPortal);
    AcmPortal article = (AcmPortal) doc;
    assertNotNull(article.getTitle());
    assertTrue(article.getTitle().contains("Exhibit"));
    assertTrue(sae.validateScholarlyArticle(article));
  }

}
