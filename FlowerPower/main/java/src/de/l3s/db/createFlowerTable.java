package de.l3s.db;

public class createFlowerTable {

	public static String creatTableSQL(String tablename)
	{
		return "CREATE TABLE `tablename` ( `id` int(11) NOT NULL AUTO_INCREMENT, `category` varchar(100) COLLATE utf8_unicode_ci NOT NULL, `document_id` varchar(200) COLLATE utf8_unicode_ci NOT NULL, `parnum` int(11) NOT NULL, `txt` longtext COLLATE utf8_unicode_ci NOT NULL, `lem_nouns` longtext COLLATE utf8_unicode_ci NOT NULL, PRIMARY KEY (`id`), FULLTEXT KEY `lem_nouns` (`lem_nouns`)) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
	}
}
