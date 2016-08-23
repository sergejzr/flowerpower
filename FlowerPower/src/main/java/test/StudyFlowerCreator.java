package test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import javax.xml.bind.JAXBException;

import de.l3s.db.DB;
import de.l3s.flower.Category;
import de.l3s.flower.Flower;
import de.l3s.flower.TopicLink;

public class StudyFlowerCreator {
	public static void main(String[] args) {

		// generateCompareCategoriesStudytables(new File("E:\\flowers\\"));
		tryDry(new File("E:\\flowers\\"));
		//generateStudytables(new File("E:\\flowers\\"));
	}

	private static void tryDry(File directory) {
		HashSet<String> floweridx = new HashSet<String>();
		for (File f : directory.listFiles()) {
			if (!f.getName().endsWith("xml"))
				continue;
			String flowername = f.getName().substring(0,
					f.getName().lastIndexOf("_"));
			if (!floweridx.contains(flowername)) {
				floweridx.add(flowername);

				Flower flower;
				try {
					flower = Flower.readFlower(f);
					TopicSample btopics = getOrderedCats(flower);
					HashSet<Integer> idx = new HashSet<Integer>();
					idx.addAll(btopics.getBest().keySet());
					idx.addAll(btopics.getRandom().keySet());
					System.out.print("Connection: ");

					for (de.l3s.flower.Connection con : flower
							.getOrderedConnections()) {
						System.out.print("("
								+ flower.getCategoryById(con.getCat1())
										.getName() + "-"+flower.getCategoryById(con.getCat2())
										.getName()+") - ");
					}
					System.out.println();
					for (Integer cur1 : idx) {
						HashSet<Integer> curconti = btopics.getBest().get(cur1);
						if (curconti == null) {
							System.out.println(" no pos pair for "
									+ flower.getCategoryById(cur1).getName());
						} else
							for (Integer cur2 : curconti) {
								System.out.println(flower.getCategoryById(cur1)
										.getName()
										+ " - "
										+ flower.getCategoryById(cur2)
												.getName() + " pos");

							}
						curconti = btopics.getRandom().get(cur1);
						if (curconti == null) {
							System.out.println(" no neg pair for "
									+ flower.getCategoryById(cur1).getName());
						} else
							for (Integer cur2 : curconti) {
								System.out.println(flower.getCategoryById(cur1)
										.getName()
										+ " - "
										+ flower.getCategoryById(cur2)
												.getName() + " neg");

							}
					}

					

				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	private static void generateCompareCategoriesStudytables(File directory) {

		try {
			Connection dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
			

			PreparedStatement pst = dbcon
					.prepareStatement("INSERT INTO "
							+ "flickrattractive.flowerpower_topicrelevance_userstudy_full_description (sid,exptype,flowerfile,categoryid,categoryname,topicid,topiclable,istruepositive) VALUES(NULL,?,?,?,?,?,?,?) ");

			PreparedStatement pstcontrol = dbcon
					.prepareStatement("INSERT INTO "
							+ "flickrattractive.flowerpower_topicrelevance_userstudy_control (flowerfile,isactive) VALUES(?,?) ");

			HashSet<String> floweridx = new HashSet<String>();

			for (File f : directory.listFiles()) {
				if (!f.getName().endsWith("xml"))
					continue;
				String flowername = f.getName().substring(0,
						f.getName().lastIndexOf("_"));
				if (!floweridx.contains(flowername)) {
					floweridx.add(flowername);
					try {
						Flower flower = Flower.readFlower(f);

						TopicSample btopics = getOrderedCats(flower);

						
						
						
						HashSet<Integer> idx = new HashSet<Integer>();
						idx.addAll(btopics.getBest().keySet());
						idx.addAll(btopics.getRandom().keySet());
					//	System.out.print("Connection: ");

						for (de.l3s.flower.Connection con : flower
								.getOrderedConnections()) {
							/*
							System.out.print("("
									+ flower.getCategoryById(con.getCat1())
											.getName() + "-"+flower.getCategoryById(con.getCat2())
											.getName()+") - ");
											*/
						}
						//System.out.println();
						for (Integer cur1 : idx) {
							HashSet<Integer> curconti = btopics.getBest().get(cur1);
							if (curconti == null) {
								/*
								System.out.println(" no pos pair for "
										+ flower.getCategoryById(cur1).getName());
										*/
							} else
								for (Integer cur2 : curconti) {
									
									pst.setString(1, "ordering");
									pst.setString(2, f.getName());
									pst.setString(3, cur1 + "");
									pst.setString(4, flower.getCategoryById(cur1)
											.getName());
									pst.setInt(5, cur2);
									pst.setString(6, flower.getCategoryById(cur2)
											.getName());
									pst.setInt(7, 1);
									pst.addBatch();
									
								

								}
							curconti = btopics.getRandom().get(cur1);
							if (curconti == null) {
								/*
								System.out.println(" no neg pair for "
										+ flower.getCategoryById(cur1).getName());
										*/
							} else
								for (Integer cur2 : curconti) {
									/*
									System.out.println(flower.getCategoryById(cur1)
											.getName()
											+ " - "
											+ flower.getCategoryById(cur2)
													.getName() + " neg");*/
									pst.setString(1, "ordering");
									pst.setString(2, f.getName());
									pst.setString(3, cur1 + "");
									pst.setString(4, flower.getCategoryById(cur1)
											.getName());
									pst.setInt(5, cur2);
									pst.setString(6, flower.getCategoryById(cur2)
											.getName());
									pst.setInt(7, 0);
									pst.addBatch();

								}
						}
						
						
						
					
						pst.executeBatch();

					} catch (JAXBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private static TopicSample getOrderedCats(Flower flower) {

		Hashtable<Integer, HashSet<Integer>> besttopics = new Hashtable<Integer, HashSet<Integer>>();
		Hashtable<Integer, HashSet<Integer>> randomtopics = new Hashtable<Integer, HashSet<Integer>>();

		class Pair {
			int x, y;
			private boolean itstrue;

			Pair(int x, int y) {
				if (x > y) {
					this.x = x;
					this.y = y;
				} else {
					this.y = x;
					this.x = y;
				}
			}

			@Override
			public String toString() {
				// TODO Auto-generated method stub
				return x + "_" + y;
			}

			public void setTrue(boolean b) {
				itstrue = b;

			}

			public boolean isTrue() {
				// TODO Auto-generated method stub
				return itstrue;
			}
		}
		List<Category> allcats = flower.getOrderedCategories();
		

		de.l3s.flower.Connection firstcon = null;
		de.l3s.flower.Connection lastcon = null;
		Hashtable<String, Pair> trueidx = new Hashtable<String, Pair>();
		
		for (de.l3s.flower.Connection con : flower.getOrderedConnections()) {
			if (firstcon == null)
				firstcon = con;

			if (lastcon != null) {
				Pair p = new Pair(lastcon.getCat1(), con.getCat1());
				p.setTrue(true);
				trueidx.put(p.toString(), p);
			}

			lastcon = con;
		}
		
		Pair lp = new Pair(lastcon.getCat1(), firstcon.getCat1());
		lp.setTrue(true);
		trueidx.put(lp.toString(), lp);
		
		for(Pair par:trueidx.values())
		{
			System.out.println(flower.getCategoryById(par.x)
					.getName()
					+ " - "
					+ flower.getCategoryById(par.y)
							.getName() + " tst");
		}
		
		
		
		lastcon.getCat2(); 
		Hashtable<String, Pair> randidx = new Hashtable<String, Pair>();

		for (Category cat1 : allcats) {
			for (Category cat2 : allcats) {
				if (cat1.getId() == cat2.getId())
					continue;
				Pair p = new Pair(cat1.getId(), cat2.getId());
				if(trueidx.containsKey(p.toString())) continue;
				randidx.put(p.toString(), p);
			}
		}
		
		
		List<Pair> randomizedpairs = new ArrayList<Pair>();
		randomizedpairs.addAll(randidx.values());
		Collections.shuffle(randomizedpairs);

		List<Pair> selectedpairs = new ArrayList<Pair>();
		for (Pair p : randomizedpairs) {
			if (trueidx.get(p.toString()) == null) {
				selectedpairs.add(p);
				p.setTrue(false);
			}
			if (selectedpairs.size() >= trueidx.size()) {
				break;
			}
		}
		randomizedpairs.addAll(trueidx.values());
		Collections.shuffle(randomizedpairs);

		Stack<Pair> pairstack = new Stack<Pair>();
		pairstack.addAll(randomizedpairs);

		while (!pairstack.empty()) {
			Pair firstpair = pairstack.peek();

			int curid = firstpair.x;
			watchstack: while (true) {
				for (Pair p : pairstack) {
					Hashtable<Integer, HashSet<Integer>> curtopics = p.isTrue() ? besttopics
							: randomtopics;

					if (p.x == curid || p.y == curid) {
						pairstack.remove(p);
						HashSet<Integer> curconti = curtopics.get(curid);
						if (curconti == null) {
							curtopics.put(curid,
									curconti = new HashSet<Integer>());
						}
						curconti.add(p.x == curid ? p.y : p.x);
						continue watchstack;
					}

				}
				break;
			}

		}

		return new TopicSample(besttopics, randomtopics);
	}

	private static void generateStudytables(File directory) {

		try {
			Connection dbcon = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
			

			PreparedStatement pst = dbcon
					.prepareStatement("INSERT INTO "
							+ "flickrattractive.flowerpower_topicrelevance_userstudy_full_description (sid,exptype,flowerfile,categoryid,categoryname,topicid,topiclable,istruepositive) VALUES(NULL,?,?,?,?,?,?,?) ");

			PreparedStatement pstcontrol = dbcon
					.prepareStatement("INSERT INTO "
							+ "flickrattractive.flowerpower_topicrelevance_userstudy_control (flowerfile,isactive) VALUES(?,?) ");

			PreparedStatement chkgen = dbcon
					.prepareStatement("SELECT flowerfile FROM "
							+ "flickrattractive.flowerpower_topicrelevance_userstudy_full_description WHERE exptype='general'  ");
			ResultSet prs = chkgen.executeQuery();
			HashSet<String> flowerid=new HashSet<String>();
			while(prs.next())
			{
				flowerid.add(prs.getString("flowerfile"));
				
			}
			prs.close();
			HashSet<String> floweridx = new HashSet<String>();

			for (File f : directory.listFiles()) {
				if (!f.getName().endsWith("xml"))
					continue;
				String flowername = f.getName().substring(0,
						f.getName().lastIndexOf("_"));
				if (!floweridx.contains(flowername)) {
					floweridx.add(flowername);
					try {
						Flower flower = Flower.readFlower(f);

						TopicSample btopics = getBestTopics(flower);
						TopicSample bcons = getBestConnections(flower);
						TopicSample bgens = getBestGeneral(flower);
						pstcontrol.setString(1, f.getName());
						pstcontrol.setInt(2, 0);
						/*
						pstcontrol.execute();
						for (Category cat : flower.getCategories()
								.getCategory()) {

							for (Integer t : btopics.getBest().get(cat.getId())) {
								pst.setString(1, "category");
								pst.setString(2, f.getName());
								pst.setString(3, cat.getId() + "");
								pst.setString(4, cat.getName());
								pst.setInt(5, t);
								pst.setString(6, flower.getTopicById(t)
										.getLable());
								pst.setInt(7, 1);
								pst.addBatch();
							}

							for (Integer t : btopics.getRandom().get(
									cat.getId())) {
								pst.setString(1, "category");
								pst.setString(2, f.getName());
								pst.setString(3, cat.getId() + "");
								pst.setString(4, cat.getName());
								pst.setInt(5, t);
								pst.setString(6, flower.getTopicById(t)
										.getLable());
								pst.setInt(7, 0);
								pst.addBatch();
							}

						}
						pst.executeBatch();
*/
						for (de.l3s.flower.Connection fcon : flower
								.getConnections().getConnection()) {
							for (Integer t : bcons.getBest()
									.get(fcon.getCat1())) {
								pst.setString(1, "connection");
								pst.setString(2, f.getName());
								pst.setString(3, fcon.getCat1() + "");
								pst.setString(
										4,
										flower.getCategoryById(fcon.getCat1())
												.getName()
												+ " - "
												+ flower.getCategoryById(
														fcon.getCat2())
														.getName());
								pst.setInt(5, t);
								pst.setString(6, flower.getTopicById(t)
										.getLable());
								pst.setInt(7, 1);
								pst.addBatch();
							}

							for (Integer t : bcons.getRandom().get(
									fcon.getCat1())) {
								pst.setString(1, "connection");
								pst.setString(2, f.getName());
								pst.setString(3, fcon.getCat1() + "");
								pst.setString(
										4,
										flower.getCategoryById(fcon.getCat1())
												.getName()
												+ " - "
												+ flower.getCategoryById(
														fcon.getCat2())
														.getName());
								pst.setInt(5, t);
								pst.setString(6, flower.getTopicById(t)
										.getLable());
								pst.setInt(7, 0);
								pst.addBatch();
							}

						}
						pst.executeBatch();
						/*
						 * flower.getTopics();
						 * 
						 * List<TopicLink> generaltopics =
						 * flower.getOrderedTopics
						 * (flower.getGeneral().getTopic(), false);
						 * for(TopicLink tl:generaltopics) {
						 * 
						 * }
						 */
if(!flowerid.contains(f.getName())){
						for (Integer t : bgens.getBest().get(0)) {
							pst.setString(1, "general");
							pst.setString(2, f.getName());
							pst.setString(3, "0");
							pst.setString(4, "General");
							pst.setInt(5, t);
							pst.setString(6, flower.getTopicById(t).getLable());
							pst.setInt(7, 1);
							pst.addBatch();
						}

						for (Integer t : bgens.getRandom().get(0)) {
							pst.setString(1, "general");
							pst.setString(2, f.getName());
							pst.setString(3, "0");
							pst.setString(4, "General");
							pst.setInt(5, t);
							pst.setString(6, flower.getTopicById(t).getLable());
							pst.setInt(7, 0);
							pst.addBatch();
						}
						pst.executeBatch();
}				

						
					} catch (JAXBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private static TopicSample getBestGeneral(Flower flower) {
		Hashtable<Integer, HashSet<Integer>> besttopics = new Hashtable<Integer, HashSet<Integer>>();
		Hashtable<Integer, HashSet<Integer>> randomtopics = new Hashtable<Integer, HashSet<Integer>>();

		List<de.l3s.flower.Topic> randomlist = new ArrayList<de.l3s.flower.Topic>();
		randomlist.addAll(flower.getTopics().getTopic());

		List<TopicLink> generaltopics = flower.getOrderedTopics(flower
				.getGeneral().getTopic(), true);

		int maxtops = 6;
		int cnt = 0;

		Integer catid;
		HashSet<Integer> ids;
		besttopics.put(catid = 0, ids = new HashSet<Integer>());
		HashSet<Integer> usedtopics = new HashSet<Integer>();
		for (TopicLink tl : generaltopics) {

			if (cnt >= maxtops)
				break;

			ids.add(tl.getTid());

			cnt++;
		}

		HashSet<Integer> idsr;
		randomtopics.put(catid = 0, idsr = new HashSet<Integer>());
		Collections.shuffle(randomlist);
		for (de.l3s.flower.Topic tl : randomlist) {
			if (ids.size() == idsr.size())
				break;
			if (usedtopics.contains(tl.getTid()))
				continue;
			idsr.add(tl.getTid());
		}

		return new TopicSample(besttopics, randomtopics);
	}

	private static TopicSample getBestTopics(Flower flower) {
		Hashtable<Integer, HashSet<Integer>> besttopics = new Hashtable<Integer, HashSet<Integer>>();
		Hashtable<Integer, HashSet<Integer>> randomtopics = new Hashtable<Integer, HashSet<Integer>>();

		List<de.l3s.flower.Topic> randomlist = new ArrayList<de.l3s.flower.Topic>();
		randomlist.addAll(flower.getTopics().getTopic());

		for (de.l3s.flower.Category fcat : flower.getCategories().getCategory()) {

			int maxtops = 6;
			int cnt = 0;

			Integer catid;
			HashSet<Integer> ids;
			besttopics.put(catid = fcat.getId(), ids = new HashSet<Integer>());

			HashSet<Integer> usedtopics = new HashSet<Integer>();
			List<TopicLink> totravers = flower.getOrderedTopics(
					fcat.getReprsentativetopic(), false);
			for (TopicLink tl : totravers) {

				if (cnt >= maxtops)
					break;

				ids.add(tl.getTid());

				cnt++;
			}

			Collections.shuffle(randomlist);
			maxtops = totravers.size();
			cnt = 0;

			HashSet<Integer> idsr;
			randomtopics.put(catid = fcat.getId(),
					idsr = new HashSet<Integer>());

			for (de.l3s.flower.Topic tl : randomlist) {
				if (ids.size() == idsr.size())
					break;
				if (usedtopics.contains(tl.getTid()))
					continue;
				idsr.add(tl.getTid());
			}

		}
		return new TopicSample(besttopics, randomtopics);
	}

	private static TopicSample getBestConnections(Flower flower) {

		Hashtable<Integer, HashSet<Integer>> besttopics = new Hashtable<Integer, HashSet<Integer>>();
		Hashtable<Integer, HashSet<Integer>> randomtopics = new Hashtable<Integer, HashSet<Integer>>();

		List<de.l3s.flower.Topic> randomlist = new ArrayList<de.l3s.flower.Topic>();
		randomlist.addAll(flower.getTopics().getTopic());

		List<de.l3s.flower.Connection> totravers = flower.getConnections()
				.getConnection();
		for (de.l3s.flower.Connection fcon : totravers) {

			int maxtops = 6;
			int cnt = 0;

			HashSet<Integer> usedtopics = new HashSet<Integer>();
			Integer catid;
			HashSet<Integer> ids;
			besttopics
					.put(catid = fcon.getCat1(), ids = new HashSet<Integer>());
			for (TopicLink tl : flower.getOrderedTopics(fcon.getTopic(), false)) {

				if (cnt >= maxtops)
					break;
				ids.add(tl.getTid());
				cnt++;
			}

			HashSet<Integer> idsr;
			randomtopics.put(catid = fcon.getCat1(),
					idsr = new HashSet<Integer>());
Collections.shuffle(randomlist);
			for (de.l3s.flower.Topic tl : randomlist) {
				if (ids.size() == idsr.size())
					break;
				if (usedtopics.contains(tl.getTid()))
					continue;
				idsr.add(tl.getTid());
			}

		}
		return new TopicSample(besttopics, randomtopics);
	}
}
