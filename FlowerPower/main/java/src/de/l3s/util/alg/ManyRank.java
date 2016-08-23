package de.l3s.util.alg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ManyRank {
	List<Pair> pairs;
	private int headsize;
	private int firstpos;
	HashSet<String> topics;
	List<ScoredItem> listofcentre= new ArrayList<ScoredItem>();
	public List<ScoredItem> getListofcentre() {
		return listofcentre;
	}
	public void setListofcentre(List<ScoredItem> listofcentre) {
		this.listofcentre = listofcentre;
	}
	public ManyRank(Collection<ArrayList<String>> collection,int size) 
	{
		HashMap<Integer, HashMap<String, Double>> scoremap= new HashMap<Integer, HashMap<String,Double>>();
		topics= new HashSet<String>();
		List<String> l1;
		ArrayList arrays = new ArrayList(collection);
		for(int i=0;i<collection.size();i++)
		{
			HashMap<String, Double> sl= new HashMap<String, Double>();
			l1=(List<String>) arrays.get(i);
			for(int j=0;j<size;j++)
			{
				sl.put(l1.get(j).trim(),(double) j);
				topics.add(l1.get(j).trim());
				
			}
			scoremap.put(i, sl);
		}
		rankLists(scoremap, size);
	}
	public int getFirstpos() {
		return firstpos;
	}
	public void rankLists(HashMap<Integer, HashMap<String, Double>> scoremap, int k) {
		
		
		pairs=new ArrayList<Pair>();
		firstpos=-1;
		listofcentre= new ArrayList<ScoredItem>();
		for(String topic: topics)
		{
			double score = 0;
			for(Integer k1: scoremap.keySet())
			{
				score+=scoremap.get(k1).get(topic);
			}
			listofcentre.add(new ScoredItem(topic, score));
		}
		Collections.sort(listofcentre, new Comparator<ScoredItem>(){

			@Override
			public int compare(ScoredItem o1, ScoredItem o2) {
				// TODO Auto-generated method stub
				return Double.valueOf(o1.score())
						.compareTo(Double.valueOf(o2.score()));
			}
			
		});
		
	}
			
			
			
			
			
	
	public int getHeadsize() {
		return headsize;
	}
public static void main(String[] args) {
	
	/*List<String> l1=Arrays.asList("sound tape sale condition offer , copy price sale book cover , borland game paradox len offer , price sale day number phone , port card drive board disk , art wolverine comic_strip hulk sabretooth , monitor apple video color screen , car clutch lock brake auto , system software version program data , phone system question protection germany , modem captain duo software buffalo , love time interference lance man , printer font print postscript page , drive disk problem system tape , apr reference nntp-posting-host sender organization , bike mile year car problem , university conference research center information , drive ide jumper controller slave , people apartment door home azerbaijani , sender university alaska apr usenet , michael georgia university research phone , bank fred pom shell larry , acces reference nntp-posting-host message-id abortion , space nasa project world flight , year admiral steve bimore jeff , scsi drive ide controller device , service anthony node network bulletin , detector radar signal radio band , list mail internet network message , picture people sleeve clinton bake , reference nntp-posting-host message-id distribution number , kinsey film reisman steel movie , machine character type phone uart , monitor launch mag japan space , circuit power chip line output , scope john price virginia dog , stanford rock request kid leland , robinson allocation unit winfield player , portal instruction cup easter cpu , washington power fan heat speed , bell bill lab jersey stewart , copy uter software user program , peny player ulf shot stick , data edge face mil surface , dog street cypriot house cypru , stance service war material bear , system morality animal keith species , apple net kent activity cheer , team vax ice dave king , rate sample voice line speech , buffalo sabre news-software eduin john , istanbul ankara turkey york university , convention karl business hes sept , monitor university apr organization elevator , andrew slip light packet driver , steve diego knife reference network , zarathushtra magus religion temple source , data model image package research , microsoft-window network mbyte server macintosh , dod problem run denizen rider , book information paper page pres , entry output file program line , bob gainey player doug gilmour , mark clutch year wilson atlantaga , truth absolute phig scripture word , armenia armenian azerbaijan azeri turkey , war island ship smith mormon , mike william john mask steve , gaza palestinian gazan israel strip , dream goal sweden peter garrett , clarkson john division ahl moncton , disease aid infection hiv lyme , adl san bullock francisco information , cleveland freenet nntp-posting-host message-id reference , jehovah father lord son mormon , heh cancer page research number , homosexuality paul sex verse man , iran program audio energy iranian , thoma people power romise time , party state candidate law election , battery x-soviet nazi hitler fact , space satellite launch vehicle year , command april test spacecraft los , phill hallam-baker power government law , muslim bosnian serb bosnia war , man callison law thing stuff , islam holocaust museum muslim qur'an , trial school witness judge youth , armenian genocide turk people armenia , ham stove day power radio , baptism parent desire wright child , medium ray lee ear rose , josephu jesu reference time jim , duke language tongue andrew andy , temperature part lab data pasadena , brian life kendig thing world , sin sinner sex love hate , universe stanford bit mov order , germany france usa april italy , physic problem mechanic quantum cartridge , gopher picture photography kirlian field , version vesselin centaur fax biochemistry , candida yeast infection quack patient , perry jim man american opinion , game roy goal montreal team , arab party jew joseph askew , university ence professor theory history , church pope priest year churche , nazi world war history bobby , day jack turkey christma woman , turk turkey greece greek village , ground wire outlet washington case , canada heh insurance system care , body people child woman soldier , study number report sex percent , colorado peace spot post jame , opinion jake livni george employer , period play shot power king , evolution john shaft fact creationist , drug war people user cigarette , water hole horizon event plant , encryption technology privacy government device , cub red york won san , stratu transfer rocket distribution reference , war kuwait iraq saudi iraqi , israel arab jew palestine question , space nasa center data satellite , president myer decision question pres , virginia islam people religion gregg , mouse board number weight line , gun police cop thing revolver , cobb tax theory mike iety , led chemistry ence light curve , batf warrant weapon agent gun , widget application function error code , school student education year work , tube type thing copper ton , patent david holland tempest information , line point gravity fact model , energy star theory larson universe , sport smith hall career steve , graphic image file format pub , argument evidence god proof statement , card driver window mode video , group vote david john graphic , mormon law religion cult government , people militia constitution arm government , government steve power people state , god jesu man bible word , image color pixel bit colormap , player year baseball bogg met , image file jpeg format gif , gun firearm rate handgun crime , apr sender net nick gmt , book text matthew word act , frank truth o'dwyer reference reality , man clayton people sex homosexual , word jim dictionary time context , israel arab lebanon village peace , book people time vice bob , jew israel people nazi land , food msg people effect restaurant , insurance abortion heh coverage tax , fbi fire koresh people evidence , earth planet moon orbit mission , ence theory fact people opinion , shuttle space station mission acces , god church scripture bible jesu , thing jon system people morality , work cost space moon year , window keyboard key machine unix , car speed road traffic guy , question friend year time answer , sun east motorcycle bmw dog , oil engine mile car fuel , religion god belief faith people , law christian people paul church , mac apple simm card chip , team player season hockey nhl , god life hell people heaven , key bit chip message number , president clinton tax job people , team game player goal leaf , window widget server application motif , god jesu christ sin lord , server sun file openwindow motif , people time year thing lot , state law government court case , image bit graphic format color , god question reason thing evidence , fire fbi child gas tank , doctor patient pain effect treatment , bike motorcycle dog rider time , key clipper phone chip government , car engine year price model , gun people crime time law , window file program problem manager , game hockey team fan espn , game year run team baseball , people time problem point thing".split("\\s*,\\s*")); 
	List<String> l2=Arrays.asList("fire fbi child gas tank , gun people crime time law , jew israel people nazi land , israel arab lebanon village peace , man clayton people sex homosexual , mormon law religion cult government , fbi fire koresh people evidence , batf warrant weapon agent gun , israel arab jew palestine question , gun firearm rate handgun crime , armenian genocide turk people armenia , insurance abortion heh coverage tax , turk turkey greece greek village , president clinton tax job people , state law government court case , people militia constitution arm government , armenia armenian azerbaijan azeri turkey , government steve power people state , muslim bosnian serb bosnia war , arab party jew joseph askew , stratu transfer rocket distribution reference , study number report sex percent , gun police cop thing revolver , opinion jake livni george employer , body people child woman soldier , president myer decision question pres , people time problem point thing , phill hallam-baker power government law , frank truth o'dwyer reference reality , drug war people user cigarette , gaza palestinian gazan israel strip , apple net kent activity cheer , cobb tax theory mike iety , ence theory fact people opinion , god jesu man bible word , word jim dictionary time context , cleveland freenet nntp-posting-host message-id reference , day jack turkey christma woman , war kuwait iraq saudi iraqi , adl san bullock francisco information , acces reference nntp-posting-host message-id abortion , nazi world war history bobby , picture people sleeve clinton bake , ham stove day power radio , party state candidate law election , school student education year work , law christian people paul church , canada heh insurance system care , university ence professor theory history , thing jon system people morality , iran program audio energy iranian , people apartment door home azerbaijani , service anthony node network bulletin , virginia islam people religion gregg , war island ship smith mormon , universe stanford bit mov order , brian life kendig thing world , zarathushtra magus religion temple source , god jesu christ sin lord , josephu jesu reference time jim , mark clutch year wilson atlantaga , people time year thing lot , battery x-soviet nazi hitler fact , baptism parent desire wright child , trial school witness judge youth , line point gravity fact model , convention karl business hes sept , colorado peace spot post jame , islam holocaust museum muslim qur'an , istanbul ankara turkey york university , evolution john shaft fact creationist , stance service war material bear , thoma people power romise time , argument evidence god proof statement , stanford rock request kid leland , dream goal sweden peter garrett , water hole horizon event plant , book text matthew word act , jehovah father lord son mormon , physic problem mechanic quantum cartridge , religion god belief faith people , kinsey film reisman steel movie , god question reason thing evidence , man callison law thing stuff , medium ray lee ear rose , steve diego knife reference network , sin sinner sex love hate , monitor university apr organization elevator , germany france usa april italy , perry jim man american opinion , dod problem run denizen rider , clarkson john division ahl moncton , microsoft-window network mbyte server macintosh , art wolverine comic_strip hulk sabretooth , bell bill lab jersey stewart , team vax ice dave king , data model image package research , church pope priest year churche , entry output file program line , buffalo sabre news-software eduin john , command april test spacecraft los , period play shot power king , heh cancer page research number , space satellite launch vehicle year , temperature part lab data pasadena , bank fred pom shell larry , love time interference lance man , god life hell people heaven , homosexuality paul sex verse man , bob gainey player doug gilmour , truth absolute phig scripture word , peny player ulf shot stick , version vesselin centaur fax biochemistry , data edge face mil surface , monitor launch mag japan space , rate sample voice line speech , year admiral steve bimore jeff , gopher picture photography kirlian field , robinson allocation unit winfield player , mouse board number weight line , system morality animal keith species , andrew slip light packet driver , player year baseball bogg met , disease aid infection hiv lyme , graphic image file format pub , dog street cypriot house cypru , candida yeast infection quack patient , image color pixel bit colormap , game roy goal montreal team , image file jpeg format gif , god church scripture bible jesu , drive ide jumper controller slave , widget application function error code , cub red york won san , ground wire outlet washington case , duke language tongue andrew andy , modem captain duo software buffalo , phone system question protection germany , portal instruction cup easter cpu , book information paper page pres , mike william john mask steve , scsi drive ide controller device , machine character type phone uart , space nasa center data satellite , patent david holland tempest information , shuttle space station mission acces , led chemistry ence light curve , question friend year time answer , energy star theory larson universe , apr sender net nick gmt , tube type thing copper ton , printer font print postscript page , sport smith hall career steve , window widget server application motif , scope john price virginia dog , sender university alaska apr usenet , detector radar signal radio band , borland game paradox len offer , encryption technology privacy government device , port card drive board disk , group vote david john graphic , copy price sale book cover , server sun file openwindow motif , key bit chip message number , book people time vice bob , team game player goal leaf , monitor apple video color screen , food msg people effect restaurant , team player season hockey nhl , michael georgia university research phone , earth planet moon orbit mission , oil engine mile car fuel , car speed road traffic guy , sun east motorcycle bmw dog , window keyboard key machine unix , work cost space moon year , car clutch lock brake auto , image bit graphic format color , space nasa project world flight , sound tape sale condition offer , reference nntp-posting-host message-id distribution number , card driver window mode video , washington power fan heat speed , drive disk problem system tape , bike mile year car problem , window file program problem manager , mac apple simm card chip , university conference research center information , price sale day number phone , circuit power chip line output , bike motorcycle dog rider time , apr reference nntp-posting-host sender organization , copy uter software user program , list mail internet network message , car engine year price model , key clipper phone chip government , doctor patient pain effect treatment , game hockey team fan espn , game year run team baseball , system software version program data".split("\\s*,\\s*")); 
	List<String> l3=Arrays.asList("".split("\\s*,\\s*"));
	ManyRank pr=new ManyRank(l1, l2,4);
	
	System.out.println("first pair position "+pr.getFirstpos());
	System.out.println("last pair position "+pr.getHeadsize());
	for(Pair p:pr.getPairs())
	{
		System.out.println(p);	
	}*/
	
}
private List<Pair> getPairs() {
return pairs;
	
}

}
